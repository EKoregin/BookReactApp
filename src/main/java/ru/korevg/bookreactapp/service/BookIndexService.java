package ru.korevg.bookreactapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.korevg.bookreactapp.dto.BookSearchDto;
import ru.korevg.bookreactapp.exceptions.BookNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookIndexService {

    private final BookService bookService;
    private final BookContentService bookContentService;
    private final ElasticsearchTemplate elasticsearchTemplate;


    public void indexBookInElasticSearch(String isbn) {
        log.info("Index book in ElasticSearch with isbn: {}", isbn);
        bookService.findByIsbn(isbn)
                .switchIfEmpty(Mono.defer(() -> {
                    String errMsg = String.format("Book with isbn %s not found", isbn);
                    log.info(errMsg);
                    return Mono.error(new BookNotFoundException(errMsg));
                }))
                .doOnNext(bookDto -> {
                    BookSearchDto dto = new BookSearchDto();
                    dto.setId(bookDto.getId());
                    dto.setTitle(bookDto.getTitle());
                    dto.setAuthor(bookDto.getAuthor());
                    dto.setIsbn(bookDto.getIsbn());
                    dto.setPrice(bookDto.getPrice());
                    elasticsearchTemplate.save(dto);
                }).subscribe();
    }


//    public void indexBookInElasticSearch(String isbn) {
//        log.info("Index book in ElasticSearch with isbn: {}", isbn);
//        bookService.findByIsbn(isbn)
//                .switchIfEmpty(Mono.defer(() -> {
//                    String errMsg = String.format("Book with isbn %s not found", isbn);
//                    log.info(errMsg);
//                    return Mono.error(new BookNotFoundException(errMsg));
//                }))
//                .flatMap(bookDto -> {
//                    var contentId = bookDto.getContent();
//                    if (contentId == null) {
//                        String errMsg = String.format("Content for book with isbn %s is null", isbn);
//                        log.info(errMsg);
//                        return Mono.error(new ContentNotFoundException(errMsg));
//                    }
//                    return bookContentService.findById(contentId)
//                            .switchIfEmpty(Mono.defer(() -> {
//                                String errMsg = String.format("Content ID: %s for book with isbn %s is null", contentId, isbn);
//                                log.info(errMsg);
//                                return Mono.error(new ContentNotFoundException(errMsg));
//                            }));
//                });
//    }
}
