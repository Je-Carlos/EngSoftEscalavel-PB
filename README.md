# Pé Sujo

Sistema de comandas para atendimento de mesas. O frontend React chama o backend Spring Boot; o backend consulta o estoque por HTTP via Eureka. Remoções e cancelamentos geram eventos RabbitMQ por outbox, consumidos pelo estoque e pela auditoria.

## Componentes

| Componente | Responsabilidade | Porta interna |
|---|---|---:|
| `frontend` | Interface e proxy da API | 80 |
| `backend` | Mesas, produtos, comandas e auditoria | 8080 |
| `estoque-service` | Saldo, baixa e estorno | 8081 |
| `service-registry` | Descoberta Eureka | 8761 |
| PostgreSQL × 2 | Dados de atendimento e estoque | 5432 |
| RabbitMQ | Distribuição de eventos | 5672 |

`eventos-contratos` é uma biblioteca compartilhada, sem processo ou imagem própria. O fluxo e os contratos estão em [arquitetura](docs/arquitetura.md), [persistência](docs/persistencia.md) e [eventos](docs/eventos.md).

## Executar com Docker Compose

Requisitos: Docker Desktop ou Docker Engine com Compose. Copie `.env.example` para `.env` e altere as senhas antes de iniciar:

```powershell
Copy-Item .env.example .env
docker compose up --build -d
docker compose ps
```

Abra o frontend em `http://localhost:3000`. A API fica em `http://localhost:8080/api`, Eureka em `http://localhost:8761` e RabbitMQ Management em `http://localhost:15672`. As portas parametrizadas em `compose.yaml` podem ser alteradas em `.env`. Para parar sem apagar os volumes: `docker compose down`.

## Testar

Os testes Java usam JDK 21, Maven e Docker ativo para PostgreSQL descartável. Configure `JAVA_HOME` para o JDK 21 e confirme com `mvn.cmd -version`. O frontend usa Node.js 22. No PowerShell, invoque `npm.cmd` para não depender da política de scripts da máquina.

```powershell
mvn.cmd -B test
npm.cmd --prefix frontend ci
npm.cmd --prefix frontend test -- --run
npm.cmd --prefix frontend run build
```

Com o Compose em execução, valide o fluxo entre backend, estoque e RabbitMQ:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validar-integracao.ps1
```

## Implantar e monitorar

O [guia de operação](docs/operacao.md) contém build individual das imagens, implantação no Kubernetes do Docker Desktop, variáveis, Secrets, escala, recuperação de Pods, consulta de logs e traces, CI/CD e roteiro da demonstração. O workflow em `.github/workflows/ci.yml` executa testes e um fluxo integrado em pull requests e em `master`; após sucesso em `master`, publica quatro imagens com a tag `sha-<commit>` no GHCR.
