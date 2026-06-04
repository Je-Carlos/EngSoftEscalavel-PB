# Pé Sujo

Sistema acadêmico de comanda aberta para um boteco brasileiro tradicional.

## Descrição

O Pé Sujo permite que um atendente visualize mesas, abra uma comanda, adicione itens consumidos, consulte o total e feche a conta. Esta entrega implementa um monólito simples com backend Spring Boot, frontend React e documentação técnica.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- H2 Database
- Maven
- React
- Vite
- Vitest

## Estrutura

```text
Pb_BarPeSujo/
  backend/
  frontend/
  docs/
  README.md
  .codex/
```

## Executar Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend: `http://localhost:8080`

H2 Console: `http://localhost:8080/h2-console`

Credenciais H2:

- JDBC URL: `jdbc:h2:mem:pesujo`
- User: `sa`
- Password: vazio

## Executar Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend: `http://localhost:5173`

## Testes e Build

Backend:

```bash
cd backend
./mvnw test
```

Frontend:

```bash
cd frontend
npm test -- --run
npm run build
```

## Funcionalidades

- Listar, cadastrar, consultar, atualizar e remover mesas.
- Alterar status de mesa.
- Listar, cadastrar, consultar, atualizar e remover produtos.
- Marcar produtos como disponíveis ou indisponíveis.
- Abrir comanda para mesa livre.
- Adicionar e remover itens de comanda.
- Consultar total da comanda.
- Fechar ou cancelar comanda.
- Interface React consumindo a API REST.

## Demonstração

Fluxo demonstrável:

1. Acessar o frontend.
2. Visualizar as mesas do salão.
3. Abrir uma comanda para uma mesa livre.
4. Escolher produtos do cardápio.
5. Adicionar itens à comanda.
6. Consultar o total da mesa.
7. Fechar a conta.
8. Confirmar que a mesa voltou para `LIVRE`.

## Documentação

A documentação arquitetural está em [`docs/arquitetura.md`](docs/arquitetura.md).
