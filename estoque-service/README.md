# Estoque Service

Serviço responsável pelo saldo dos produtos. Ele registra-se no Eureka e usa um PostgreSQL próprio.

```bash
mvn -f estoque-service/pom.xml spring-boot:run
```

API: `http://localhost:8081/api/estoques`.
