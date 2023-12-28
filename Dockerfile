FROM openjdk:17

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} courseservice.jar

ENTRYPOINT ["java", "-jar", "/courseservice.jar"]

EXPOSE 8087