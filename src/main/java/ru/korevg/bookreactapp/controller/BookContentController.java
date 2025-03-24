package ru.korevg.bookreactapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.korevg.bookreactapp.dto.BookContentDto;
import ru.korevg.bookreactapp.service.BookContentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books/content")
public class BookContentController {

    private final BookContentService bookContentService;

    @PostMapping(value = "/{isbn}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<BookContentDto> uploadBookContent(
            @PathVariable String isbn,
            @RequestPart("content") Flux<DataBuffer> content) {
        return bookContentService.create(content, isbn);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<byte[]>> downloadBookContentById(@PathVariable("id") Long id) {
        return bookContentService.findById(id)
                .map(bk -> ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.parseMediaType(bk.getMediaType()))
                        .body(bk.getContent()));
    }

    @GetMapping("/isbn/{isbn}")
    public Mono<ResponseEntity<byte[]>> downloadBookContentByIsbn(@PathVariable("isbn") String isbn) {
        return bookContentService.findByIsbn(isbn)
                .map(bk -> ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.parseMediaType(bk.getMediaType()))
                        .body(bk.getContent()));
    }

}
