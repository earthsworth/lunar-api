FROM gradle:8.12.1-jdk21 AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle

RUN gradle --no-daemon dependencies

COPY . .
RUN gradle --no-daemon clean bootJar

FROM amazoncorretto:21.0.6-al2023-headless
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
