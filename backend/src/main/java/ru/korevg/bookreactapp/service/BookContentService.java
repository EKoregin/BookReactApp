package ru.korevg.bookreactapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.korevg.bookreactapp.exceptions.BookNotFoundException;
import ru.korevg.bookreactapp.producer.BookIndexProducer;
import ru.korevg.bookreactapp.repository.BookRepository;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookContentService {

    private final BookIndexProducer bookIndexProducer;
    private final BookRepository bookRepository;
    private final S3Service s3Service;

    /**
     * Метод принимает DataBuffer преобразовывает в массив byte[].
     * Создает сущность BookContent и сохраняет в БД
     *
     * @return Mono<BookContentDto>
     */
    @Transactional
    public Mono<String> create(MultipartFile content, String isbn) {
        log.info("Upload BookContent...");
        return bookRepository.findByIsbn(isbn)
                .switchIfEmpty(Mono.defer(() -> {
                    String errMsg = String.format("Book with isbn %s not found", isbn);
                    log.info(errMsg);
                    return Mono.error(new BookNotFoundException(errMsg));
                }))
                .flatMap(book -> {
                    int size = (int) content.getSize();

                    if (book.getContent() != null) {
                        log.info("Update BookContent for book with isbn {}", isbn);
                        log.info("Delete old BookContent with keyFile {}", book.getContent());
                        s3Service.deleteFile(book.getContent());
                    }
                    String fileKey = s3Service.uploadFile(content.getOriginalFilename(), content);
                    log.info("BookContent was uploaded with size: {} and media type: {}", size, content.getContentType());

                    return Mono.just(fileKey)
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(savedBookContent -> {
                                log.info("Save book isbn: {} with fileKey: {}", isbn, fileKey);
                                book.setContent(fileKey);
                                bookRepository.save(book).subscribe();
                                //Отправка сообщения на индексацию книги
                                try {
                                    String msg = String.format("Book with isbn: {%s} need reindex in ElasticSearch", isbn);
                                    log.info(msg);
                                    bookIndexProducer.sendMessage(msg);
                                } catch (AmqpConnectException e) {
                                    log.error("Ошибка подключения к RabbitMQ: {}", e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            });
                }).onErrorMap(IOException.class, e -> new RuntimeException("Проблема с обработкой данных", e));
    }
}
