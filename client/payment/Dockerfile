FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/payment-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

EXPOSE 19080