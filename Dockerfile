FROM eclipse-temurin:17-jdk
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources /src/main/resources
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
