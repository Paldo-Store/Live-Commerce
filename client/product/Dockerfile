FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/product-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]