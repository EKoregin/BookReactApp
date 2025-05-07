package ru.korevg.bookreactapp.it;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
//@SpringBootTest
public class ITBase {

    @Container
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:17.4");

    @Container
    static final RabbitMQContainer RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine")
            .withEnv("RABBITMQ_DEFAULT_USER", "book")
            .withEnv("RABBITMQ_DEFAULT_PASS", "password");

    @Container
    static final ElasticsearchContainer ELASTICSEARCH_CONTAINER = new ElasticsearchContainer("elasticsearch:7.17.0");


    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" +
                POSTGRES_CONTAINER.getHost() + ":" + POSTGRES_CONTAINER.getFirstMappedPort()
        + "/" + POSTGRES_CONTAINER.getDatabaseName());
        registry.add("spring.r2dbc.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES_CONTAINER::getPassword);
        //flyway connection
        registry.add("spring.flyway.url", () -> "jdbc:postgresql://" +
                POSTGRES_CONTAINER.getHost() + ":" + POSTGRES_CONTAINER.getFirstMappedPort()
                + "/" + POSTGRES_CONTAINER.getDatabaseName());
        registry.add("spring.flyway.user", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.flyway.password", POSTGRES_CONTAINER::getPassword);
        POSTGRES_CONTAINER.waitingFor(Wait.forHealthcheck());
    }

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT_MQ_CONTAINER::getHost);
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getFirstMappedPort);
    }

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.rest.uris", () -> "http://" +
                ELASTICSEARCH_CONTAINER.getHost() + ":" + ELASTICSEARCH_CONTAINER.getFirstMappedPort()
        );
        ELASTICSEARCH_CONTAINER.waitingFor(Wait.forHttp("/").forStatusCode(200));
    }
}
