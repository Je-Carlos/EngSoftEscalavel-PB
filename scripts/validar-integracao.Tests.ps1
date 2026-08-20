$validador = Join-Path $PSScriptRoot 'validar-integracao.ps1'

Describe 'validar-integracao.ps1' {
    It 'valida Eureka, baixa e estorno de estoque' {
        $saida = & $validador 2>&1

        ($saida -join "`n") | Should Match 'Integração validada'
    }
}
