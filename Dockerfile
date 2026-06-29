# Stage 1: Build
FROM eclipse-temurin:17-jdk-focal AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

COPY src src

RUN ./mvnw package -DskipTests


# Stage 2: Run
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
