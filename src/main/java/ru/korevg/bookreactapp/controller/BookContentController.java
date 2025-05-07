package ru.korevg.bookreactapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.korevg.bookreactapp.service.BookContentService;
import ru.korevg.bookreactapp.service.S3Service;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/books/content")
public class BookContentController {

    private final BookContentService bookContentService;
    private final S3Service s3Service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{isbn}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> uploadBookContent(
            @PathVariable String isbn,
            @RequestPart("content") MultipartFile content) {
        return bookContentService.create(content, isbn);
    }

    @GetMapping
    public Mono<ResponseEntity<byte[]>> getBookContent(@RequestParam(name = "key") String fileKey) {
        log.info("Get Book Content for key {}", fileKey);
        return Mono.just(ResponseEntity
                .status(HttpStatus.OK)
                .body(s3Service.downloadFile(fileKey))
        );
    }
}
