FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
COPY config-repo ./config-repo
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 18080
