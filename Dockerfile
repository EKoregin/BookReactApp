FROM openjdk:17-alpine
ADD target/*.jar app.jar

ENV DB_HOST=localhost:5432
ENV RABBITMQ_HOST=localhost
ENV ELASTIC_HOST=localhost

ENTRYPOINT [ "sh", "-c", "java \
    -Dspring.profiles.active=cloud \
    -jar /app.jar \
    --cloud.dbase-host=$DB_HOST \
    --cloud.rabbitmq-host=$RABBITMQ_HOST \
    --cloud.elastic-host=$ELASTIC_HOST \
    "]