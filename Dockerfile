FROM gradle:8.12.1-jdk21 AS build

WORKDIR /app
ENV GRADLE_USER_HOME=/root/.gradle

COPY gradle.properties .
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN gradle dependencies --no-daemon || true

COPY src ./src

RUN gradle bootJar --no-daemon

FROM openjdk:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/
CMD ["sh", "-c", "java -jar $(ls *.jar)"]
