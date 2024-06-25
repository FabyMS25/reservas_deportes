
FROM adoptopenjdk/openjdk11
VOLUME /temp
EXPOSE 9096:8080
ARG JAR_FILE=target/licencias-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app-licencias.jar
ENTRYPOINT ["java","-jar","/app-licencias.jar"]
