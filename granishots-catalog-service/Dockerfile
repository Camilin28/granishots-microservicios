FROM gradle:8.7-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle build -x test

FROM eclipse-temurin:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "granishots-catalog-service-0.0.1-SNAPSHOT.jar"]