FROM eclipse-temurin:11-jdk-jammy AS build

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src src
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/build/libs/app.jar app.jar

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD curl --fail --silent http://127.0.0.1:8085/ws-notificaciones/info > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
