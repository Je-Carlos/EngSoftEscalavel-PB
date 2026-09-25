FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY eventos-contratos eventos-contratos
COPY backend backend
COPY estoque-service estoque-service
COPY service-registry service-registry
ARG MODULE
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl ${MODULE} -am -DskipTests package && \
    mvn -q dependency:copy -Dartifact=io.opentelemetry.javaagent:opentelemetry-javaagent:2.31.1 \
      -DoutputDirectory=/otel -Dmdep.stripVersion=true

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE
COPY --from=build --chown=10001:0 /app/${MODULE}/target/*.jar app.jar
COPY --from=build --chown=10001:0 /otel/opentelemetry-javaagent.jar /opt/otel/opentelemetry-javaagent.jar
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
