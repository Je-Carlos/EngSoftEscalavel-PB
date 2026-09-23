param(
    [string]$RabbitUser = $env:RABBITMQ_USER,
    [string]$RabbitPassword = $env:RABBITMQ_PASSWORD
)

$ErrorActionPreference = 'Stop'
if (-not $RabbitUser -or -not $RabbitPassword) { throw 'Defina RABBITMQ_USER e RABBITMQ_PASSWORD.' }
$auth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("${RabbitUser}:$RabbitPassword"))
$headers = @{ Authorization = "Basic $auth" }
$eventId = [guid]::NewGuid().ToString()
$comandaId = 987654321
$payload = @{
    eventId = $eventId
    eventType = 'ItemRemovido'
    occurredAt = [DateTimeOffset]::UtcNow.ToString('o')
    aggregateId = $comandaId
    itens = @(@{ itemId = 1; produtoId = 999999999; quantidade = 1 })
} | ConvertTo-Json -Depth 5 -Compress
$body = @{
    properties = @{ content_type = 'application/json'; delivery_mode = 2 }
    routing_key = 'comanda.item.removido'
    payload = $payload
    payload_encoding = 'string'
} | ConvertTo-Json -Depth 5
$publicado = Invoke-RestMethod 'http://localhost:15672/api/exchanges/%2F/comandas.eventos/publish' `
    -Method Post -Headers $headers -ContentType 'application/json' -Body $body
if (-not $publicado.routed) { throw 'Evento não foi roteado.' }

$limite = (Get-Date).AddSeconds(45)
do {
    $fila = Invoke-RestMethod 'http://localhost:15672/api/queues/%2F/comandas.estoque.dlq' -Headers $headers
    $auditoria = Invoke-RestMethod "http://localhost:8080/api/eventos/comandas/$comandaId"
    if ($fila.messages -gt 0 -and ($auditoria | Where-Object eventId -eq $eventId)) { break }
    Start-Sleep -Seconds 1
} while ((Get-Date) -lt $limite)
if ($fila.messages -le 0) { throw 'Evento não chegou à DLQ do estoque.' }
if (-not ($auditoria | Where-Object eventId -eq $eventId)) { throw 'Auditoria não recebeu o evento.' }
Write-Output "DLQ do estoque e auditoria validadas para eventId=$eventId"
