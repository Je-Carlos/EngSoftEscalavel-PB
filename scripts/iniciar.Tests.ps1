$launcher = Join-Path $PSScriptRoot 'iniciar.ps1'

Describe 'iniciar.ps1' {
    It 'encontra o JDK 21 sem JAVA_HOME configurado manualmente' {
        $javaHomeOriginal = $env:JAVA_HOME

        try {
            Remove-Item Env:\JAVA_HOME -ErrorAction SilentlyContinue
            $saida = & $launcher -Verificar 2>&1

            $LASTEXITCODE | Should Be 0
            ($saida -join "`n") | Should Match 'JDK 21 encontrado'
        }
        finally {
            if ($null -eq $javaHomeOriginal) {
                Remove-Item Env:\JAVA_HOME -ErrorAction SilentlyContinue
            }
            else {
                $env:JAVA_HOME = $javaHomeOriginal
            }
        }
    }
}
