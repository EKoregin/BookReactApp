package ru.korevg.bookreactapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.korevg.bookreactapp.dto.BookSearchDto;
import ru.korevg.bookreactapp.exceptions.BookNotFoundException;
import ru.korevg.bookreactapp.exceptions.ContentNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookIndexService {

    private final BookService bookService;
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final BookContentService bookContentService;

    public void indexBookInElasticSearch(String isbn) {
        log.info("Index book in ElasticSearch with isbn: {}", isbn);
        bookService.findByIsbn(isbn)
                .switchIfEmpty(Mono.defer(() -> {
                    String errMsg = String.format("Book with isbn %s not found", isbn);
                    log.info(errMsg);
                    return Mono.error(new BookNotFoundException(errMsg));
                }))
                .flatMap(bookDto -> {
                    BookSearchDto dto = new BookSearchDto();
                    dto.setId(bookDto.getId());
                    dto.setTitle(bookDto.getTitle());
                    dto.setAuthor(bookDto.getAuthor());
                    dto.setIsbn(bookDto.getIsbn());
                    dto.setPrice(bookDto.getPrice());

                    var contentId = bookDto.getContent();
                    if (contentId == null) {
                        log.info("Content for book with isbn {} is null. Index without content", isbn);
                        elasticsearchTemplate.save(dto);
                        return Mono.just(dto);
                    }

                    return bookContentService.findById(contentId)
                            .switchIfEmpty(Mono.defer(() -> {
                                String errMsg = String.format("Content ID: %s for book with isbn %s not found. Index without content", contentId, isbn);
                                log.info(errMsg);
                                elasticsearchTemplate.save(dto);
                                return Mono.error(new ContentNotFoundException(errMsg));
                            }))
                            .flatMap(bookContent -> {
                                try {
                                    log.info("Tokenize book content");
                                    var rawString = extractText(bookContent.getContent());
                                    var tokens = tokenizeText(rawString);
                                    log.info("Size of book content's tokens: {}", tokens.size());
                                    dto.setTokens(tokens);
                                    dto.setContent(rawString);
                                    elasticsearchTemplate.save(dto);
                                    return Mono.just(dto);
                                } catch (IOException e) {
                                    return Mono.error(new RuntimeException(e));
                                }
                            });
                }).subscribe();
    }

    private String extractText(byte[] content) throws IOException {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Content byte array cannot be null or empty");
        }
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        Parser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();

        try (ByteArrayInputStream stream = new ByteArrayInputStream(content)) {
            parser.parse(stream, handler, metadata, context);
        } catch (Exception e) {
            throw new IOException("Error parsing content", e);
        }

        log.info("Metadata: {}", metadata);

        return handler.toString();
    }

    private Set<String> tokenizeText(String text) {
        return new HashSet<>(Arrays.stream(text.split("\\W+"))
                .toList());
    }
}
