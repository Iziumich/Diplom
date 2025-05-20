FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r appuser && \
    useradd -r -g appuser appuser && \
    mkdir -p /app/uploads && \
    chown -R appuser:appuser /app/uploads

COPY --from=builder /app/target/*.jar app.jar
COPY src/main/resources/application.yml ./config/

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/,file:/app/config/application.yml"]
