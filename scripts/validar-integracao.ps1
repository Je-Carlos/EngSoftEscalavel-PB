$ErrorActionPreference = 'Stop'
$api = 'http://localhost:8080/api'

$limiteInicio = (Get-Date).AddSeconds(60)
do {
    try {
        Invoke-RestMethod "$api/estoques" | Out-Null
        $estoquesProntos = $true
        break
    } catch {
        Start-Sleep -Seconds 1
    }
} while ((Get-Date) -lt $limiteInicio)
if (-not $estoquesProntos) { throw 'Backend ainda não consegue acessar o estoque.' }

$produto = @((Invoke-RestMethod "$api/produtos")) | Select-Object -First 1
$mesa = @((Invoke-RestMethod "$api/mesas")) | Where-Object status -eq 'LIVRE' | Select-Object -First 1
if (-not $mesa) { throw 'Não há mesa livre para a demonstração.' }

$saldoInicial = 3
Invoke-RestMethod "$api/estoques/$($produto.id)" -Method Put -ContentType 'application/json' -Body "{`"quantidade`":$saldoInicial}" | Out-Null
$comanda = Invoke-RestMethod "$api/comandas/abrir" -Method Post -ContentType 'application/json' -Body "{`"mesaId`":$($mesa.id)}"
Invoke-RestMethod "$api/comandas/$($comanda.id)/itens" -Method Post -ContentType 'application/json' -Body "{`"produtoId`":$($produto.id),`"quantidade`":1}" | Out-Null

$estoques = @((Invoke-RestMethod "$api/estoques"))
$saldoAposBaixa = ($estoques | Where-Object produtoId -eq $produto.id).quantidade
if ($saldoAposBaixa -ne 2) { throw "Baixa não aplicada: saldo $saldoAposBaixa." }

Invoke-RestMethod "$api/comandas/$($comanda.id)/cancelar" -Method Patch | Out-Null
$limite = (Get-Date).AddSeconds(45)
do {
    $estoques = @((Invoke-RestMethod "$api/estoques"))
    $saldoAposEstorno = ($estoques | Where-Object produtoId -eq $produto.id).quantidade
    $eventos = @((Invoke-RestMethod "$api/eventos/comandas/$($comanda.id)"))
    if ($saldoAposEstorno -eq $saldoInicial -and ($eventos | Where-Object eventType -eq 'ComandaCancelada')) { break }
    Start-Sleep -Milliseconds 500
} while ((Get-Date) -lt $limite)
if ($saldoAposEstorno -ne $saldoInicial) { throw "Estorno não aplicado: saldo $saldoAposEstorno." }
if (-not ($eventos | Where-Object eventType -eq 'ComandaCancelada')) { throw 'Evento de auditoria ausente.' }

Write-Output 'Integração validada: Eureka, baixa, estorno assíncrono e auditoria.'
