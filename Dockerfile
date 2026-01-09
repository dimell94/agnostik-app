FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy source and build the bootJar
COPY . .
RUN chmod +x ./gradlew && ./gradlew clean bootJar --no-daemon

# Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/build/libs/agnostik-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
