FROM amazoncorretto:21.0.6-al2023-headless
WORKDIR /app
CMD ["./gradlew", "clean", "bootJar"]
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar $(ls *.jar)"]
