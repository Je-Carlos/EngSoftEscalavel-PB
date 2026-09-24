param(
    [string]$ImageTag,
    [string]$EnvFile = '.env',
    [string]$Context = 'docker-desktop'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$manifests = Join-Path $root 'k8s/app'

if (-not $ImageTag) {
    $ImageTag = "sha-$(git -C $root rev-parse HEAD)"
}

if ($ImageTag -notmatch '^sha-[0-9a-f]{7,40}$') {
    throw 'ImageTag deve ter o formato sha-<hash Git>.'
}
if (-not [IO.Path]::IsPathRooted($EnvFile)) {
    $EnvFile = Join-Path $root $EnvFile
}
$EnvFile = (Resolve-Path -LiteralPath $EnvFile).Path
$required = @('POSTGRES_DB', 'POSTGRES_USER', 'POSTGRES_PASSWORD', 'ESTOQUE_POSTGRES_DB', 'ESTOQUE_POSTGRES_USER', 'ESTOQUE_POSTGRES_PASSWORD', 'RABBITMQ_USER', 'RABBITMQ_PASSWORD', 'GRAFANA_ADMIN_PASSWORD')
$entries = @{}
foreach ($line in [IO.File]::ReadAllLines($EnvFile)) {
    if ($line -match '^\s*([A-Z][A-Z0-9_]*)=(.*)$') {
        $entries[$Matches[1]] = $Matches[2].Trim()
    }
}
foreach ($key in $required) {
    if (-not $entries[$key] -or $entries[$key] -eq 'troque-esta-senha') {
        throw "Configure $key em $EnvFile antes de implantar."
    }
}

& kubectl --context $Context apply -f (Join-Path $manifests 'namespace.yaml')
if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar namespace.' }

$secret = & kubectl --context $Context -n pesujo create secret generic pesujo-credentials "--from-env-file=$EnvFile" --dry-run=client -o yaml
if ($LASTEXITCODE -ne 0) { throw 'Falha ao gerar Secret.' }
$secret | & kubectl --context $Context apply -f -
if ($LASTEXITCODE -ne 0) { throw 'Falha ao aplicar Secret.' }

foreach ($file in @('config.yaml', 'data.yaml')) {
    & kubectl --context $Context apply -f (Join-Path $manifests $file)
    if ($LASTEXITCODE -ne 0) { throw "Falha ao aplicar $file." }
}

& kubectl --context $Context apply -f (Join-Path $root 'k8s/observability/stack.yaml')
if ($LASTEXITCODE -ne 0) { throw 'Falha ao aplicar observabilidade.' }

foreach ($name in @('postgres', 'postgres-estoque', 'rabbitmq')) {
    & kubectl --context $Context -n pesujo rollout status "statefulset/$name" --timeout=300s
    if ($LASTEXITCODE -ne 0) { throw "StatefulSet $name não iniciou." }
}
foreach ($name in @('loki', 'tempo', 'alloy', 'grafana')) {
    & kubectl --context $Context -n pesujo rollout status "deployment/$name" --timeout=300s
    if ($LASTEXITCODE -ne 0) { throw "Observabilidade $name não iniciou." }
}

$services = [IO.File]::ReadAllText((Join-Path $manifests 'services.yaml')).Replace('IMAGE_TAG', $ImageTag)
$services | & kubectl --context $Context apply -f -
if ($LASTEXITCODE -ne 0) { throw 'Falha ao aplicar Services e Deployments.' }

foreach ($name in @('service-registry', 'backend', 'estoque-service', 'frontend')) {
    & kubectl --context $Context -n pesujo rollout status "deployment/$name" --timeout=300s
    if ($LASTEXITCODE -ne 0) { throw "Deployment $name não iniciou." }
}

Write-Host "Implantação $ImageTag pronta. Acesse com: kubectl --context $Context -n pesujo port-forward service/frontend 8088:80"
