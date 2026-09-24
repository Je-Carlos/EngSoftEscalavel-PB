# Operação e demonstração

## Arquitetura implantada

O frontend serve a interface e encaminha `/api` ao backend. O backend usa Eureka para localizar o estoque, PostgreSQL para comandas e RabbitMQ para publicar eventos gravados em outbox. O estoque tem PostgreSQL próprio. Os eventos de remoção e cancelamento alimentam o estorno e a auditoria; a baixa ao adicionar item é HTTP síncrono. Consulte [eventos.md](eventos.md) para o contrato e os cenários de recuperação.

| Serviço | Porta do contêiner | Exposição local no Compose |
|---|---:|---:|
| Frontend | 80 | `FRONTEND_PORT`, padrão 3000 |
| Backend | 8080 | 8080 |
| Estoque | 8081 | 8081 |
| Eureka | 8761 | 8761 |
| PostgreSQL atendimento | 5432 | `POSTGRES_PORT`, padrão 5432 |
| PostgreSQL estoque | 5432 | `ESTOQUE_POSTGRES_PORT`, padrão 5433 |
| RabbitMQ | 5672 e 15672 | `RABBITMQ_PORT` e `RABBITMQ_MANAGEMENT_PORT` |

## Configuração e Docker

Requisitos: Docker Desktop com Compose; para testes locais, JDK 21, Maven e Node.js 22. Configure `JAVA_HOME` para o JDK 21 e confirme com `mvn.cmd -version`. No PowerShell, use `npm.cmd` caso a política de scripts bloqueie `npm.ps1`. Copie `.env.example` para `.env` e troque as senhas. `.env` é ignorado pelo Git. `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `ESTOQUE_POSTGRES_DB`, `ESTOQUE_POSTGRES_USER`, `ESTOQUE_POSTGRES_PASSWORD`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD` e `GRAFANA_ADMIN_PASSWORD` são usados na implantação; as variáveis terminadas em `_PORT` e `FRONTEND_PORT` controlam portas locais. Os hosts internos são os nomes dos serviços e vêm dos manifests ou do Compose. Nenhuma credencial deve entrar nos Dockerfiles, manifests ou commits.

```powershell
Copy-Item .env.example .env
docker compose up --build -d
docker compose ps
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validar-integracao.ps1
```

Para construir imagens separadamente a partir da raiz do projeto:

```powershell
docker build --build-arg MODULE=backend -t pesujo-backend:local .
docker build --build-arg MODULE=estoque-service -t pesujo-estoque-service:local .
docker build --build-arg MODULE=service-registry -t pesujo-service-registry:local .
docker build -t pesujo-frontend:local ./frontend
```

O contrato Maven `eventos-contratos` entra nas imagens Java durante o build e não é um processo separado. `docker compose logs -f backend estoque-service` acompanha falhas locais. `docker compose down` para os contêineres sem remover dados; volumes são removidos apenas se essa exclusão for desejada.

## Kubernetes no Docker Desktop

Ative o Kubernetes no Docker Desktop e confirme `kubectl config current-context` igual a `docker-desktop`. O ambiente demonstrado usa o cluster local com provisionador kind e o namespace `pesujo`. Crie `.env` antes da implantação. O script valida as variáveis obrigatórias, cria o Secret `pesujo-credentials` a partir desse arquivo e aplica os manifests. Configuração sem segredo fica em ConfigMap. PostgreSQL e RabbitMQ usam volumes persistentes.

Após o pipeline publicar uma versão, substitua `COMMIT_SHA` abaixo pelo hash completo do commit exibido na execução do workflow:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/implantar-k8s.ps1 -ImageTag sha-COMMIT_SHA
kubectl -n pesujo get pods,services,pvc
kubectl -n pesujo rollout status deployment/backend
kubectl -n pesujo rollout status deployment/estoque-service
kubectl -n pesujo port-forward service/frontend 8088:80
```

O parâmetro `-ExecutionPolicy Bypass` permite executar o script sem alterar a política de scripts da máquina.

Para demonstrar uma branch ainda não publicada no GHCR, construa localmente as quatro imagens com a tag do commit atual antes de chamar `implantar-k8s.ps1`. O Docker Desktop com provisionador kind precisa estar usando o armazenamento de imagens containerd compartilhado:

```powershell
$tag = "sha-$(git rev-parse HEAD)"
docker build --build-arg MODULE=backend -t "ghcr.io/je-carlos/pesujo-backend:$tag" .
docker build --build-arg MODULE=estoque-service -t "ghcr.io/je-carlos/pesujo-estoque-service:$tag" .
docker build --build-arg MODULE=service-registry -t "ghcr.io/je-carlos/pesujo-service-registry:$tag" .
docker build -t "ghcr.io/je-carlos/pesujo-frontend:$tag" ./frontend
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/implantar-k8s.ps1 -ImageTag $tag
```

Abra `http://localhost:8088`. Para executar o roteiro integrado em outro terminal, encaminhe a API e rode o script:

```powershell
kubectl -n pesujo port-forward service/backend 8080:8080
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validar-integracao.ps1
```

Após a primeira inicialização, quando mesas e produtos já estiverem cadastrados, demonstre escala e recuperação:

```powershell
kubectl -n pesujo scale deployment/backend --replicas=2
kubectl -n pesujo rollout status deployment/backend
kubectl -n pesujo get pods -l app=backend
kubectl -n pesujo scale deployment/backend --replicas=1
```

Reinicie um Pod do backend pela interface do Docker Desktop ou com `kubectl -n pesujo rollout restart deployment/backend`, aguarde `rollout status` e repita o roteiro integrado. O serviço volta pelo Deployment; os dados ficam nos volumes dos bancos. Para remover a demonstração, `kubectl delete namespace pesujo` também exclui os recursos e pode remover os dados associados aos volumes locais.

## Logs e rastreamento

Os serviços Java escrevem JSON no console com nome do serviço, nível, mensagem e contexto do trace. Os logs do cluster são coletados pelo Alloy e enviados ao Loki. O agente OpenTelemetry envia os traces ao Tempo; as duas fontes são consultadas no Grafana. `trace_id` liga a requisição do backend à chamada HTTP do estoque. Nos eventos assíncronos, `eventId` liga publicação, consumo e eventual erro, inclusive quando a entrega ocorre após o fim da requisição original.

Abra o Grafana por `kubectl -n pesujo port-forward service/grafana 3000:3000` e acesse `http://localhost:3000` com usuário `admin` e a senha `GRAFANA_ADMIN_PASSWORD` de `.env`. No Explore, a fonte Loki permite a consulta `{namespace="pesujo",service="backend"} | json`; a fonte Tempo recebe o `trace_id` para consulta do trace.

Para investigar uma operação, execute o roteiro de integração, consulte o trace HTTP no Grafana, copie o `trace_id` e procure as linhas de backend e estoque no Loki. Para estorno, copie o `eventId` do log do publicador e localize a confirmação de consumo ou o erro; confira também `GET /api/eventos/comandas/{id}`. As filas de erro e os passos de republicação estão em [eventos.md](eventos.md).

## CI/CD e testes

`.github/workflows/ci.yml` roda em pull requests e pushes para `master`, além de execução manual. Os jobs `java` e `frontend` executam testes e build; `system` sobe o Compose e verifica baixa, estorno e auditoria; `images` constrói as quatro imagens após essas verificações. Um comando que falha encerra o job com falha. Em push para `master`, `images` publica no GHCR com tags `sha-<commit>` e `latest`, usando o token temporário do próprio workflow. O cluster local é implantado manualmente com a tag imutável.

Ative o gate local com `git config core.hooksPath .githooks`. Ele rejeita espaços inválidos no diff e arquivos de ambiente preparados para commit; a suíte completa roda nos comandos abaixo e no workflow.

Na primeira publicação, ajuste a visibilidade de cada pacote GHCR para **público** nas configurações do repositório e confirme um `docker pull` sem login. O GHCR cria novos pacotes privados por padrão. Nenhuma credencial de registry precisa ser gravada no repositório. Para evidenciar a detecção de falhas, rode o workflow numa branch de validação com um teste intencionalmente falhando, confirme o job vermelho e corrija o teste em um novo commit.

Testes locais:

```powershell
mvn.cmd -B test
npm.cmd --prefix frontend ci
npm.cmd --prefix frontend test -- --run
npm.cmd --prefix frontend run build
docker compose up --build -d
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validar-integracao.ps1
```

## Roteiro da apresentação

1. Mostrar os serviços e os fluxos HTTP e RabbitMQ em [arquitetura.md](arquitetura.md) e [eventos.md](eventos.md).
2. Construir as quatro imagens, exibir `docker compose ps` e executar `validar-integracao.ps1`.
3. Aplicar os manifests no Docker Desktop, exibir Pods, Services e PVCs, abrir o frontend e repetir o fluxo.
4. Escalar o backend para duas réplicas, reiniciá-lo e confirmar que volta a atender.
5. Localizar o trace backend → estoque no Grafana, filtrar logs pelo `trace_id` e acompanhar um estorno por `eventId`.
6. Exibir os commits, a execução bem-sucedida do workflow, as imagens `sha-<commit>` no GHCR e uma execução que falhou por teste.
