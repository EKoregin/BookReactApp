package ru.korevg.bookreactapp.it;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.korevg.bookreactapp.dto.BookDTO;

import java.math.BigDecimal;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookItTest extends ITBase {

    private static final String TEST_BOOK_ISBN = "9781503280786";
    private static final String NEW_BOOK_ISBN = "\"978-5-4461-1107-7\"";

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
    @Order(1)
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

    @Test
    @Order(2)
    public void createBook() {
        BookDTO newBook = new BookDTO();
        newBook.setTitle("Философия Java");
        newBook.setAuthor("Эккель Брюс");
        newBook.setIsbn(NEW_BOOK_ISBN);
        newBook.setPrice(BigDecimal.valueOf(3130));

        String json = "{\n" +
                "  \"id\": 11,\n" +
                "  \"title\": \"Философия Java\",\n" +
                "  \"author\": \"Эккель Брюс\",\n" +
                "  \"isbn\": \"" + NEW_BOOK_ISBN + "\",\n" +
                "  \"price\": 3130.0,\n" +
                "  \"content\": null\n" +
                "}";

        webClient.post()
                .uri("/books")
                .bodyValue(newBook)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().json(json);
    }

    @Test
    @Order(3)
    public void updateBook() {
        BookDTO newBook = new BookDTO();
        newBook.setTitle("Moby Dick");
        newBook.setAuthor("Herman Melville");
        newBook.setIsbn(TEST_BOOK_ISBN);
        newBook.setPrice(BigDecimal.valueOf(20.99));

        String json = "{\n" +
                "  \"id\": 10,\n" +
                "  \"title\": \"Moby Dick\",\n" +
                "  \"author\": \"Herman Melville\",\n" +
                "  \"isbn\": \"9781503280786\",\n" +
                "  \"price\": 20.99,\n" +
                "  \"content\": null\n" +
                "}";

        webClient.put()
                .uri("/books/isbn/" + TEST_BOOK_ISBN)
                .bodyValue(newBook)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(json);
    }

    @Test
    @Order(4)
    public void deleteBook() {
        webClient.delete().uri("/books/isbn/" + TEST_BOOK_ISBN)
                .exchange()
                .expectStatus().isOk();
        webClient.get().uri("/books/isbn/" + TEST_BOOK_ISBN)
                .exchange()
                .expectStatus().isNotFound();
    }
}
