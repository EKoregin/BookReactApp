package ru.korevg.bookreactapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.korevg.bookreactapp.dto.BookDTO;
import ru.korevg.bookreactapp.dto.BookSearchDto;
import ru.korevg.bookreactapp.service.BookService;
import ru.korevg.bookreactapp.service.ElasticSearchService;

import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final ElasticSearchService elasticSearchService;


    @Operation(summary = "Полнотекстовый поиск фразами")
    @GetMapping("/fulltextsearch/{search}")
    public Flux<BookSearchDto> findByFullTextSearch(
            @Parameter(description = "Строка поиска")
            @PathVariable("search") String search) {
        return elasticSearchService.fullTextSearch(search);
    }

    @GetMapping("/search/{search}")
    public Flux<BookSearchDto> findBySearchString(@PathVariable("search") String search) {
        return elasticSearchService.searchBooks(search);
    }

    @GetMapping
    public Flux<BookDTO> getBooks() {
        log.info("Get all books");
        return bookService.findAll();
    }

    @GetMapping("/isbn/{isbn}")
    public Mono<ResponseEntity<BookDTO>> getBookByIsbn(@PathVariable("isbn") String isbn) {
        return bookService.findByIsbn(isbn)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Book with isbn {} not found", isbn);
                    return Mono.just(ResponseEntity.notFound().build());
                }));
    }

    @PostMapping
    public Mono<ResponseEntity<BookDTO>> createBook(@RequestBody BookDTO dto) {
        return bookService.create(dto)
                .map(book -> {
                    log.info("Book added: {}", book);
                    return ResponseEntity.created(URI.create("/isbn/" + book.getIsbn()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(book);
                })
                .onErrorResume(error -> {
                    log.error("Error adding book: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(null));
                });
    }

    @DeleteMapping("/isbn/{isbn}")
    public Mono<ResponseEntity<Void>> deleteBook(@PathVariable("isbn") String isbn) {
        return bookService.delete(isbn)
                .map(ResponseEntity::ok)
                .onErrorResume(errors -> {
                    log.error(errors.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    @PutMapping("/isbn/{isbn}")
    public Mono<ResponseEntity<BookDTO>> updateBook(@PathVariable("isbn") String isbn,
                                                    @RequestBody BookDTO dto) {
        return bookService.update(isbn, dto)
                .map(ResponseEntity::ok)
                .onErrorResume(errors -> {
                    log.error(errors.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
}
