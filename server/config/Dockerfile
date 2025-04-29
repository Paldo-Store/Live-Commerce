FROM openjdk:17-jdk-slim
WORKDIR /app

COPY build/libs/*.jar configserver.jar
COPY config-repo /app/config-repo

ENTRYPOINT ["java", "-jar", "configserver.jar"]
EXPOSE 18080
