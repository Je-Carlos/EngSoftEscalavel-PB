param(
    [string]$PostgresUser = 'pesujo',
    [string]$PostgresDb = 'pesujo'
)

$ErrorActionPreference = 'Stop'
$api = 'http://localhost:8080/api'
$produto = (Invoke-RestMethod "$api/produtos")[0]
$mesas = Invoke-RestMethod "$api/mesas"
$mesa = $mesas | Where-Object status -eq 'LIVRE' | Select-Object -First 1
if (-not $mesa) { throw 'Não há mesa livre.' }
Invoke-RestMethod "$api/estoques/$($produto.id)" -Method Put -ContentType 'application/json' -Body '{"quantidade":3}' | Out-Null
$comanda = Invoke-RestMethod "$api/comandas/abrir" -Method Post -ContentType 'application/json' -Body "{`"mesaId`":$($mesa.id)}"
$comItem = Invoke-RestMethod "$api/comandas/$($comanda.id)/itens" -Method Post -ContentType 'application/json' -Body "{`"produtoId`":$($produto.id),`"quantidade`":1}"

docker compose --env-file .env.example stop rabbitmq | Out-Null
try {
    Invoke-RestMethod "$api/comandas/$($comanda.id)/itens/$($comItem.itens[0].id)" -Method Delete | Out-Null
    $pendentes = docker compose --env-file .env.example exec -T postgres psql -U $PostgresUser -d $PostgresDb -tAc "select count(*) from eventos_outbox where aggregate_id = $($comanda.id) and published_at is null"
    if ([int]$pendentes -ne 1) { throw "Outbox não ficou pendente: $pendentes" }
} finally {
    docker compose --env-file .env.example start rabbitmq | Out-Null
}

$limite = (Get-Date).AddSeconds(60)
do {
    $estoques = Invoke-RestMethod "$api/estoques"
    $saldo = ($estoques | Where-Object produtoId -eq $produto.id).quantidade
    $eventos = Invoke-RestMethod "$api/eventos/comandas/$($comanda.id)"
    if ($saldo -eq 3 -and ($eventos | Where-Object eventType -eq 'ItemRemovido')) { break }
    Start-Sleep -Seconds 1
} while ((Get-Date) -lt $limite)
if ($saldo -ne 3 -or -not ($eventos | Where-Object eventType -eq 'ItemRemovido')) {
    throw 'Estorno e auditoria não convergiram após reiniciar o broker.'
}
Write-Output "Recuperação do broker validada para comanda=$($comanda.id)"
