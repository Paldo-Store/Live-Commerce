FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y curl
WORKDIR /app

COPY build/libs/config-0.0.1-SNAPSHOT.jar configserver.jar
COPY config-repo /app/config-repo

EXPOSE 18080
ENTRYPOINT ["java", "-jar", "configserver.jar"]

