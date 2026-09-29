# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Resolve dependencies first so they are cached until pom.xml changes
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
    && cp target/tracker-*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S vault && adduser -S -G vault -H vault

# Copy layers from least to most frequently changing for better image caching
COPY --from=build --chown=vault:vault /workspace/extracted/dependencies/ ./
COPY --from=build --chown=vault:vault /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=vault:vault /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=vault:vault /workspace/extracted/application/ ./

USER vault
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"UP"' || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
