FROM eclipse-temurin:21

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew build -x test

EXPOSE 8080

CMD ["java", "-jar", "build/libs/granishots-catalog-service-0.0.1-SNAPSHOT.jar"]