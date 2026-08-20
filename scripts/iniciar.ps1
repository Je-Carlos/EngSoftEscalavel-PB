[CmdletBinding()]
param(
    [switch]$Verificar
)

$ErrorActionPreference = 'Stop'
$raizProjeto = Split-Path -Parent $PSScriptRoot

function Test-Jdk21 {
    param([string]$Caminho)

    $java = Join-Path $Caminho 'bin\java.exe'
    $javac = Join-Path $Caminho 'bin\javac.exe'
    if (-not (Test-Path $java) -or -not (Test-Path $javac)) {
        return $false
    }

    $versao = (& $java -version 2>&1 | Select-Object -First 1)
    return $versao -match 'version "21(?:\.|\")'
}

function Encontrar-Jdk21 {
    $candidatos = [System.Collections.Generic.List[string]]::new()
    if ($env:JAVA_HOME) {
        $candidatos.Add($env:JAVA_HOME)
    }

    @(
        'C:\Program Files\Amazon Corretto',
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Java'
    ) | Where-Object { Test-Path $_ } | ForEach-Object {
        Get-ChildItem -Path $_ -Directory -Filter 'jdk*21*' | ForEach-Object {
            $candidatos.Add($_.FullName)
        }
    }

    foreach ($candidato in $candidatos | Select-Object -Unique) {
        if (Test-Jdk21 $candidato) {
            return $candidato
        }
    }

    throw 'JDK 21 não encontrado. Instale um JDK 21 ou defina JAVA_HOME para ele.'
}

function Testar-Comando {
    param([string]$Nome)

    if (-not (Get-Command $Nome -ErrorAction SilentlyContinue)) {
        throw "Comando obrigatório não encontrado: $Nome"
    }
}

function Mostrar-Portas {
    foreach ($porta in 8761, 8081, 8080, 5173) {
        $processos = Get-NetTCPConnection -LocalPort $porta -State Listen -ErrorAction SilentlyContinue |
            Select-Object -ExpandProperty OwningProcess -Unique
        if ($processos) {
            Write-Output "Porta $porta ocupada pelo PID $($processos -join ', ')."
        }
        else {
            Write-Output "Porta $porta disponível."
        }
    }
}

function Liberar-Porta {
    param([int]$Porta)

    $processos = Get-NetTCPConnection -LocalPort $Porta -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($processo in $processos) {
        Write-Output "Encerrando PID $processo na porta $Porta."
        Stop-Process -Id $processo -Force
    }

    $limite = (Get-Date).AddSeconds(10)
    while ((Get-NetTCPConnection -LocalPort $Porta -State Listen -ErrorAction SilentlyContinue) -and (Get-Date) -lt $limite) {
        Start-Sleep -Milliseconds 250
    }

    if (Get-NetTCPConnection -LocalPort $Porta -State Listen -ErrorAction SilentlyContinue) {
        throw "Não foi possível liberar a porta $Porta."
    }
}

function Importar-Env {
    param([string]$Arquivo)

    Get-Content $Arquivo | ForEach-Object {
        if ($_ -match '^\s*([A-Z_][A-Z0-9_]*)=(.*)$') {
            Set-Item -Path "Env:$($Matches[1])" -Value $Matches[2]
        }
    }
}

function Aguardar-Http {
    param(
        [string]$Url,
        [string]$Nome
    )

    $limite = (Get-Date).AddSeconds(90)
    do {
        try {
            Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2 | Out-Null
            return
        }
        catch {
            Start-Sleep -Seconds 1
        }
    } while ((Get-Date) -lt $limite)

    throw "$Nome não respondeu em $Url após 90 segundos."
}

function Abrir-Servico {
    param(
        [string]$Titulo,
        [string]$Comando
    )

    $script = "`$Host.UI.RawUI.WindowTitle = '$Titulo'; $Comando"
    Start-Process -FilePath 'pwsh.exe' -WorkingDirectory $raizProjeto -ArgumentList @('-Command', $script) | Out-Null
}

$jdk21 = Encontrar-Jdk21
$env:JAVA_HOME = $jdk21
$env:Path = "$jdk21\bin;$env:Path"
Write-Output "JDK 21 encontrado: $jdk21"

Testar-Comando docker
Testar-Comando npm
& docker version --format '{{.Server.Version}}' | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Desktop não está disponível.'
}

if ($Verificar) {
    Mostrar-Portas
}
else {
    $envArquivo = Join-Path $raizProjeto '.env'
    if (-not (Test-Path $envArquivo)) {
        Copy-Item (Join-Path $raizProjeto '.env.example') $envArquivo
        Write-Output '.env criado a partir de .env.example.'
    }
    Importar-Env $envArquivo

    foreach ($porta in 8761, 8081, 8080, 5173) {
        Liberar-Porta $porta
    }

    Push-Location $raizProjeto
    try {
        & docker compose up -d --wait
        if ($LASTEXITCODE -ne 0) {
            throw 'Não foi possível iniciar o banco de dados com Docker Compose.'
        }
    }
    finally {
        Pop-Location
    }

    $maven = Join-Path $raizProjeto 'backend\mvnw.cmd'
    Abrir-Servico 'Pé Sujo - Eureka' "& '$maven' -f 'service-registry\pom.xml' spring-boot:run"
    Aguardar-Http 'http://localhost:8761/eureka/apps' 'Eureka'

    Abrir-Servico 'Pé Sujo - Estoque' "& '$maven' -f 'estoque-service\pom.xml' spring-boot:run"
    Aguardar-Http 'http://localhost:8081/api/estoques' 'Serviço de estoque'

    Abrir-Servico 'Pé Sujo - Backend' "& '$maven' -f 'backend\pom.xml' spring-boot:run"
    Aguardar-Http 'http://localhost:8080/api/mesas' 'Backend'

    if (-not (Test-Path (Join-Path $raizProjeto 'frontend\node_modules'))) {
        Push-Location (Join-Path $raizProjeto 'frontend')
        try {
            & npm ci
            if ($LASTEXITCODE -ne 0) {
                throw 'Não foi possível instalar as dependências do frontend.'
            }
        }
        finally {
            Pop-Location
        }
    }

    Abrir-Servico 'Pé Sujo - Frontend' "Set-Location 'frontend'; npm run dev"
    Aguardar-Http 'http://localhost:5173' 'Frontend'

    Write-Output 'Aplicação pronta em http://localhost:5173'
    Write-Output 'Validação integrada: .\scripts\validar-integracao.ps1'
}
