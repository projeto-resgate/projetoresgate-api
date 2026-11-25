FROM maven:3.9.12-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp clean package -DskipTests \
    && java -Djarmode=tools -jar target/*.jar extract --layers --destination target/extracted \
    && mv target/extracted/application/*.jar target/extracted/application/app.jar

FROM eclipse-temurin:24-jre-alpine-3.22

LABEL org.opencontainers.image.title="projetoresgate-api" \
      org.opencontainers.image.description="API do Projeto Resgate"

RUN addgroup -S app && adduser -S -G app -h /app app

WORKDIR /app

COPY --from=build --chown=app:app /build/target/extracted/dependencies/ ./
COPY --from=build --chown=app:app /build/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /build/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /build/target/extracted/application/ ./

USER app

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
