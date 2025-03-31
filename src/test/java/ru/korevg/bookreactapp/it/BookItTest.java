package ru.korevg.bookreactapp.it;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookItTest extends ITBase {

    private static final String TEST_BOOK_ISBN = "9781503280786";

    @LocalServerPort
    private int port;

    private WebTestClient webClient;

    @BeforeEach
    public void setup() {
        this.webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    public void findBook() {
        String json = "{\n" +
                "  \"id\": 10,\n" +
                "  \"title\": \"Moby Dick\",\n" +
                "  \"author\": \"Herman Melville\",\n" +
                "  \"isbn\": \"9781503280786\",\n" +
                "  \"price\": 15.99,\n" +
                "  \"content\": null\n" +
                "}";

        webClient.get().uri("/books/isbn/" + TEST_BOOK_ISBN)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(json);
    }
}
