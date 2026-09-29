FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src src

RUN ./gradlew test bootJar --no-daemon


# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install --yes --no-install-recommends fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system deportes \
    && useradd --system --gid deportes --home-dir /app deportes

COPY --from=builder --chown=deportes:deportes /app/build/libs/app.jar app.jar

USER deportes

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]