# === Сборка jar ===
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :client:build --no-daemon -x test

# === Сборка docker образа ===
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/client/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]