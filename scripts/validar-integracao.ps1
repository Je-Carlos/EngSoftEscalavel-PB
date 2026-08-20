$ErrorActionPreference = 'Stop'
$api = 'http://localhost:8080/api'

if ((Invoke-WebRequest 'http://localhost:8761/eureka/apps' -UseBasicParsing).Content -notmatch 'ESTOQUE-SERVICE') {
    throw 'estoque-service não está registrado no Eureka.'
}

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
$estoques = @((Invoke-RestMethod "$api/estoques"))
$saldoAposEstorno = ($estoques | Where-Object produtoId -eq $produto.id).quantidade
if ($saldoAposEstorno -ne $saldoInicial) { throw "Estorno não aplicado: saldo $saldoAposEstorno." }

Write-Output 'Integração validada: Eureka, baixa e estorno de estoque.'
