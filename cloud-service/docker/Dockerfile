# Сборка
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app

# Копируем pom.xml
COPY pom.xml .

# Скачиваем зависимости
RUN mvn dependency:go-offline

# Копируем исходный код
COPY src ./src

# Собираем проект
RUN mvn package -DskipTests

# Запуск
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Создаем пользователя
RUN groupadd -r appuser && \
    useradd -r -g appuser appuser && \
    mkdir -p /app/uploads && \
    chown -R appuser:appuser /app/uploads

# Копируем артефакты
COPY --from=builder /app/target/*.jar app.jar
COPY src/main/resources/application.yml ./config/

# Пользователь и порт
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/,file:/app/config/application.yml"]