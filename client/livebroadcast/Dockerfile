# 베이스 이미지: JDK 17
FROM openjdk:17-jdk-slim

# 작업 디렉토리 생성
WORKDIR /app

# 빌드된 JAR 복사 (Gradle 기준, build/libs에 생김)
COPY build/libs/livebroadcast-0.0.1-SNAPSHOT.jar app.jar
COPY .env .env

# 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 19060
