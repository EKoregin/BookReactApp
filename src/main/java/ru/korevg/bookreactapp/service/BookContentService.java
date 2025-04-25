package ru.korevg.bookreactapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.korevg.bookreactapp.domain.BookContent;
import ru.korevg.bookreactapp.dto.BookContentDto;
import ru.korevg.bookreactapp.exceptions.BookNotFoundException;
import ru.korevg.bookreactapp.exceptions.ContentNotFoundException;
import ru.korevg.bookreactapp.mapper.BookContentMapper;
import ru.korevg.bookreactapp.producer.BookIndexProducer;
import ru.korevg.bookreactapp.repository.BookContentRepository;
import ru.korevg.bookreactapp.repository.BookRepository;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookContentService {


    private final BookContentRepository bookContentRepository;
    private final BookContentMapper bookContentMapper;
    private final BookIndexProducer bookIndexProducer;
    private final BookRepository bookRepository;

    /**
     * Метод принимает DataBuffer преобразовывает в массив byte[].
     * Создает сущность BookContent и сохраняет в БД
     *
     * @return Mono<BookContentDto>
     */
    @Transactional
    public Mono<BookContentDto> create(MultipartFile content, String isbn) {
        log.info("Upload BookContent...");
        return bookRepository.findByIsbn(isbn)
                .switchIfEmpty(Mono.defer(() -> {
                    String errMsg = String.format("Book with isbn %s not found", isbn);
                    log.info(errMsg);
                    return Mono.error(new BookNotFoundException(errMsg));
                }))
                .flatMap(book -> {
                    int size = (int) content.getSize();
                    byte[] byteContent;
                    try {
                        byteContent = content.getBytes();
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException(e));
                    }

                    BookContent bookContent = new BookContent();
                    if (book.getContent() != null) {
                        log.info("Update BookContent with id {}", book.getContent());
                        bookContent.setId(book.getContent());
                    }
                    bookContent.setContent(byteContent);
                    bookContent.setSize(size);
                    bookContent.setMediaType(getMediaTypeFromByteArray(byteContent));
                    log.info("BookContent was uploaded with size: {} and media type: {}", size, bookContent.getMediaType());

                    return bookContentRepository.save(bookContent)
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(savedBookContent -> {
                                log.info("Save book isbn: {} with contentId: {}", isbn, savedBookContent.getId());
                                book.setContent(savedBookContent.getId());
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
                            })
                            .map(bookContentMapper::toBookContentDto);
                }).onErrorMap(IOException.class, e -> new RuntimeException("Проблема с обработкой данных", e));
    }

//    /**
//     * Метод принимает DataBuffer преобразовывает в массив byte[].
//     * Создает сущность BookContent и сохраняет в БД
//     *
//     * @return Mono<BookContentDto>
//     */
//    @Transactional
//    public Mono<BookContentDto> create(Flux<DataBuffer> content, String isbn) {
//        log.info("Upload BookContent...");
//        return bookRepository.findByIsbn(isbn)
//                .switchIfEmpty(Mono.defer(() -> {
//                    String errMsg = String.format("Book with isbn %s not found", isbn);
//                    log.info(errMsg);
//                    return Mono.error(new BookNotFoundException(errMsg));
//                }))
//                .flatMap(book -> DataBufferUtils.join(content)
//                        .flatMap(dataBuffer -> {
//                            int size = dataBuffer.readableByteCount();
//                            byte[] byteContent = new byte[size];
//                            dataBuffer.read(byteContent);
//
//                            BookContent bookContent = new BookContent();
//                            if (book.getContent() != null) {
//                                log.info("Update BookContent with id {}", book.getContent());
//                                bookContent.setId(book.getContent());
//                            }
//                            bookContent.setContent(byteContent);
//                            bookContent.setSize(size);
//                            bookContent.setMediaType(getMediaTypeFromByteArray(byteContent));
//                            log.info("BookContent was uploaded with size: {} and media type: {}", size, bookContent.getMediaType());
//
//                            return bookContentRepository.save(bookContent)
//                                    .publishOn(Schedulers.boundedElastic())
//                                    .doOnNext(savedBookContent -> {
//                                        log.info("Save book isbn: {} with contentId: {}", isbn, savedBookContent.getId());
//                                        book.setContent(savedBookContent.getId());
//                                        bookRepository.save(book).subscribe();
//                                        //Отправка сообщения на индексацию книги
//                                        try {
//                                            String msg = String.format("Book with isbn: {%s} need reindex in ElasticSearch", isbn);
//                                            log.info(msg);
//                                            bookIndexProducer.sendMessage(msg);
//                                        } catch (AmqpConnectException e) {
//                                            log.error("Ошибка подключения к RabbitMQ: {}", e.getMessage());
//                                            throw new RuntimeException(e);
//                                        }
//                                    })
//                                    .map(bookContentMapper::toBookContentDto);
//                        })
//                ).onErrorMap(IOException.class, e -> new RuntimeException("Проблема с обработкой данных", e));
//    }

    public static String getMediaTypeFromByteArray(byte[] byteArray) {
        Tika tika = new Tika();
        return tika.detect(byteArray);
    }

    public Mono<BookContent> findById(Long id) {
        return bookContentRepository.findById(id);
    }

    public Mono<BookContent> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .switchIfEmpty(Mono.defer(() -> {
                    String errMsg = String.format("Book with isbn %s not found", isbn);
                    log.info(errMsg);
                    return Mono.error(new BookNotFoundException(errMsg));
                }))
                .flatMap(book -> {
                    var contentId = book.getContent();
                    if (contentId == null) {
                        String errMsg = String.format("Content for book with isbn %s is null", isbn);
                        log.info(errMsg);
                        return Mono.error(new ContentNotFoundException(errMsg));
                    }
                    return bookContentRepository.findById(contentId)
                            .switchIfEmpty(Mono.defer(() -> {
                                String errMsg = String.format("Content ID: %s for book with isbn %s is null", contentId, isbn);
                                log.info(errMsg);
                                return Mono.error(new ContentNotFoundException(errMsg));
                            }));
                });
    }

    @Transactional
    public Mono<Void> deleteById(Long id) {
        return bookContentRepository.deleteById(id);
    }
}
