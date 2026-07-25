# syntax=docker/dockerfile:1
FROM gradle:8.14.3-jdk21 AS build

ARG SERVICE
ARG OTEL_AGENT_VERSION=2.30.0
ARG OTEL_AGENT_SHA256=9d6bc2ad8dd8fb7f730984988e57b8ac0a82d81c7b3b8ae795378718733a509d

WORKDIR /workspace

COPY settings.gradle build.gradle ./
COPY inventory-service/build.gradle inventory-service/build.gradle
COPY order-service/build.gradle order-service/build.gradle
COPY inventory-service/src inventory-service/src
COPY order-service/src order-service/src

RUN gradle ":${SERVICE}:bootJar" --no-daemon \
    && curl -fsSL \
        "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" \
        -o /tmp/opentelemetry-javaagent.jar \
    && echo "${OTEL_AGENT_SHA256}  /tmp/opentelemetry-javaagent.jar" | sha256sum -c -

FROM eclipse-temurin:21-jre-jammy

ARG SERVICE

RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app --create-home app

WORKDIR /app

COPY --from=build /tmp/opentelemetry-javaagent.jar /opt/opentelemetry-javaagent.jar
COPY --from=build "/workspace/${SERVICE}/build/libs/${SERVICE}.jar" /app/app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/opt/opentelemetry-javaagent.jar", "-jar", "/app/app.jar"]

