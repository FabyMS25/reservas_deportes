
FROM adoptopenjdk/openjdk11
VOLUME /temp
EXPOSE 9096:8080
ARG JAR_FILE=target/app-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

FROM openjdk:11-jdk
WORKDIR /app
COPY build/libs/*.jar /app.jar

COPY build/libs/*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]