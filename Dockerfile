# Этап 1: Подготовка
FROM openjdk:17-alpine AS builder
WORKDIR /app
# Укажите точное имя JAR или убедитесь, что он существует
COPY target/*.jar app.jar
# Проверка наличия файла (для отладки)
RUN ls -l /app

# Этап 2: Минимальный образ
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /app/app.jar .
# Установка прав
RUN chmod 644 /app/app.jar
# Проверка наличия файла (для отладки)
RUN ls -l /app

# Переменные окружения
ENV DB_HOST=localhost:5432 \
    RABBITMQ_HOST=localhost \
    ELASTIC_HOST=localhost \
    KEYCLOAK_URL=http://localhost:8081 \
    LOGSTASH_HOST=localhost \
    MINIO_HOST=localhost \
    USER_HOST=localhost \
    SPRING_PROFILES_ACTIVE=cloud

ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Оптимизированный ENTRYPOINT
ENTRYPOINT ["java", "-jar", "/app/app.jar", \
    "--cloud.dbase-host=${DB_HOST}", \
    "--cloud.rabbitmq-host=${RABBITMQ_HOST}", \
    "--cloud.elastic-host=${ELASTIC_HOST}", \
    "--cloud.keycloak-url=${KEYCLOAK_URL}", \
    "--cloud.logstash-host=${LOGSTASH_HOST}", \
    "--cloud.minio-host=${MINIO_HOST}", \
    "--cloud.user-host=${USER_HOST}"]
