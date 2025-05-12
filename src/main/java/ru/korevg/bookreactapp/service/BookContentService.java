package ru.korevg.bookreactapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.korevg.bookreactapp.exceptions.BookNotFoundException;
import ru.korevg.bookreactapp.producer.BookIndexProducer;
import ru.korevg.bookreactapp.repository.BookRepository;

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
     * @param filePartFlux downloading file
     * @param isbn         updating book
     * @return Mono<BookContentDto>
     */
    public Mono<String> create(Flux<FilePart> filePartFlux, String isbn) {
        log.info("Upload BookContent...");
        return bookRepository.findByIsbn(isbn)
                .switchIfEmpty(Mono.defer(() -> {
                    String errMsg = String.format("Book with isbn %s not found", isbn);
                    log.info(errMsg);
                    return Mono.error(new BookNotFoundException(errMsg));
                }))
                .flatMap(book -> filePartFlux.next().flatMap(filePart -> {
                    String contentType = filePart.headers().getContentType() != null
                            ? filePart.headers().getContentType().toString()
                            : "application/octet-stream";

                    return DataBufferUtils.join(filePart.content())
                            .map(dataBuffer -> {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                return bytes;
                            })
                            .flatMap(bytes -> {
                                if (book.getContent() != null) {
                                    log.info("Delete old BookContent with keyFile {}", book.getContent());
                                    s3Service.deleteFile(book.getContent());
                                }

                                return s3Service.uploadFile(filePart.filename(), bytes, contentType)
                                        .flatMap(fileKey -> {
                                            log.info("BookContent was uploaded with size: {} and media type: {}",
                                                    bytes.length, contentType);
                                            book.setContent(fileKey);
                                            return bookRepository.save(book)
                                                    .flatMap(savedBook -> {
                                                        String msg = String.format("Book with isbn: {%s} need reindex in ElasticSearch", isbn);
                                                        log.info(msg);
                                                        return bookIndexProducer.sendMessage(msg).thenReturn(fileKey);
                                                    });
                                        });
                            });
                }))
                .onErrorResume(e -> {
                    log.error("Error processing file upload for ISBN {}: {}", isbn, e.getMessage());
                    return Mono.error(new RuntimeException("Failed to process file upload", e));
                });
    }
}
