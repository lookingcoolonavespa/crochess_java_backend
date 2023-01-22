FROM maven:3.8.7-openjdk-18-slim AS builder
COPY pom.xml /app/
COPY src /app/src
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/pom.xml clean package \
    -DskipTests

#Run
FROM openjdk:18-slim
COPY --from=builder /app/target/crochess_backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]