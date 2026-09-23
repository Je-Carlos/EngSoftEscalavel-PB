# Pé Sujo

Sistema de comanda aberta para um boteco brasileiro tradicional, com backend Spring Boot, microsserviço de estoque e frontend React.

## Sobre o projeto

O Pé Sujo ajuda no atendimento de mesas em um pequeno bar. Pela interface, o atendente acompanha o salão, abre uma comanda para uma mesa livre, adiciona produtos consumidos, consulta o total e fecha a conta.

A aplicação mantém o atendimento em camadas e separa o saldo de produtos no microsserviço de estoque. Remoções e cancelamentos publicam eventos RabbitMQ por outbox.

## Arquitetura

A descrição da arquitetura, dos domínios, dos endpoints e dos diagramas está em [docs/arquitetura.md](docs/arquitetura.md). A configuração de banco e migrações está em [docs/persistencia.md](docs/persistencia.md). O fluxo de eventos, a consistência eventual e os cenários de demonstração estão em [docs/eventos.md](docs/eventos.md).

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- PostgreSQL 17
- Flyway
- Hibernate Envers
- Maven
- React
- Vite
- Vitest
- Spring Cloud Netflix Eureka e OpenFeign
- RabbitMQ e Spring AMQP

## Estrutura do repositório

```text
Pb_BarPeSujo/
  backend/
  estoque-service/
  service-registry/
  eventos-contratos/
  frontend/
  docs/
  README.md
```

## Funcionalidades

- Cadastro, listagem, consulta, atualização e remoção de mesas.
- Alteração de status de mesa.
- Cadastro, listagem, consulta, atualização e remoção de produtos.
- Controle de disponibilidade dos produtos.
- Abertura de comanda para mesa livre.
- Adição e remoção de itens da comanda.
- Cálculo do total da comanda.
- Fechamento e cancelamento de comanda.
- Interface React integrada à API REST.

## Regras principais

- Uma mesa não pode ter mais de uma comanda aberta.
- Uma comanda fechada ou cancelada não recebe novos itens.
- Produto indisponível não pode ser lançado na comanda.
- Comanda vazia não pode ser fechada.
- Ao abrir uma comanda, a mesa fica `OCUPADA`.
- Ao fechar ou cancelar uma comanda, a mesa volta para `LIVRE`.

## Como executar

### Serviços

```bash
cp .env.example .env
docker compose up --build -d
```

O Compose inicia PostgreSQL, RabbitMQ Management, Eureka, backend e estoque. Troque as senhas em `.env` antes de usar fora do ambiente local. Para executar via Maven, use JDK 21 e `mvn -pl backend,estoque-service -am install -DskipTests` para instalar o contrato compartilhado; então inicie os serviços com as variáveis de `.env`.

Endereços:

- API: `http://localhost:8080`
- Estoque: `http://localhost:8081`
- Eureka: `http://localhost:8761`
- RabbitMQ Management: `http://localhost:15672` (credenciais de `.env`)
- PostgreSQL: `localhost:5432` (variáveis em `.env`)

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Endereço:

- Frontend: `http://localhost:5173`

## Testes

Backend e estoque (JDK 21 e Docker ativos):

```bash
mvn test
```

Frontend:

```bash
cd frontend
npm test -- --run
```

Build do frontend:

```bash
cd frontend
npm run build
```

Validação integrada, após iniciar o Compose:

```powershell
./scripts/validar-integracao.ps1
./scripts/validar-recuperacao.ps1
./scripts/validar-dlq.ps1 -RabbitUser pesujo -RabbitPassword <senha-do-.env>
```

## Fluxo de demonstração

1. Acessar o frontend.
2. Visualizar as mesas do salão.
3. Selecionar uma mesa livre para abrir uma comanda.
4. Escolher produtos do cardápio.
5. Adicionar itens à comanda.
6. Conferir o total.
7. Fechar a conta.
8. Confirmar que a mesa voltou para `LIVRE`.
