FROM openjdk:17-alpine
ADD target/*.jar app.jar

ENV DB_HOST=localhost:5432
ENV RABBITMQ_HOST=localhost
ENV ELASTIC_HOST=localhost
ENV KEYCLOAK_URL=http://localhost:8081
ENV LOGSTASH_HOST=localhost
ENV MINIO_HOST=localhost
ENV USER_HOST=localhost

ENTRYPOINT [ "sh", "-c", "java \
    -Dspring.profiles.active=cloud \
    -jar /app.jar \
    --cloud.dbase-host=$DB_HOST \
    --cloud.rabbitmq-host=$RABBITMQ_HOST \
    --cloud.elastic-host=$ELASTIC_HOST \
    --cloud.keycloak-url=$KEYCLOAK_URL \
    --cloud.logstash-host=$LOGSTASH_HOST \
    --cloud.minio-host=$MINIO_HOST \
    --cloud.user-host=$USER_HOST \
    "]