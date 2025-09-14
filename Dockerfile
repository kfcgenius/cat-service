FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean install -DskipTests

FROM openjdk:21

WORKDIR /app

COPY --from=build /app/target/cat-service-0.0.1.jar app.jar

CMD ["java", "-jar", "app.jar"]
