# Pé Sujo

Sistema de comanda aberta para um boteco brasileiro tradicional, desenvolvido como monólito simples com Spring Boot e React.

## Sobre o projeto

O Pé Sujo ajuda no atendimento de mesas em um pequeno bar. Pela interface, o atendente acompanha o salão, abre uma comanda para uma mesa livre, adiciona produtos consumidos, consulta o total e fecha a conta.

A aplicação foi organizada para a primeira entrega acadêmica do projeto: backend em camadas, API REST, frontend consumindo o backend e documentação técnica.

## Arquitetura

A descrição da arquitetura, dos domínios, dos endpoints e dos diagramas está em [docs/arquitetura.md](docs/arquitetura.md). A configuração de banco, migrações e auditoria está em [docs/persistencia.md](docs/persistencia.md).

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

## Estrutura do repositório

```text
Pb_BarPeSujo/
  backend/
  estoque-service/
  service-registry/
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
docker compose up -d
mvn -f service-registry/pom.xml spring-boot:run
mvn -f estoque-service/pom.xml spring-boot:run
mvn -f backend/pom.xml spring-boot:run
```

Use JDK 21. Inicie os serviços nessa ordem para que `backend` e `estoque-service` se registrem no Eureka.

Endereços:

- API: `http://localhost:8080`
- Estoque: `http://localhost:8081`
- Eureka: `http://localhost:8761`
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

Backend:

```bash
cd backend
./mvnw test
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

Validação integrada, após iniciar os três serviços:

```powershell
./scripts/validar-integracao.ps1
```

Teste do lançador:

## Fluxo de demonstração

1. Acessar o frontend.
2. Visualizar as mesas do salão.
3. Selecionar uma mesa livre para abrir uma comanda.
4. Escolher produtos do cardápio.
5. Adicionar itens à comanda.
6. Conferir o total.
7. Fechar a conta.
8. Confirmar que a mesa voltou para `LIVRE`.
