package ru.korevg.bookreactapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.korevg.bookreactapp.domain.BookContent;
import ru.korevg.bookreactapp.dto.BookContentDto;
import ru.korevg.bookreactapp.mapper.BookContentMapper;
import ru.korevg.bookreactapp.repository.BookContentRepository;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookContentService {


    private final BookContentRepository bookContentRepository;
    private final BookContentMapper bookContentMapper;

    /**
     * Метод принимает DataBuffer преобразовывает в массив byte[].
     * Создает сущность BookContent и сохраняет в БД
     * @return Mono<BookContentDto>
     */
    public Mono<BookContentDto> create(Flux<DataBuffer> content) {
        log.info("Upload BookContent...");
        return DataBufferUtils.join(content)
                .flatMap(dataBuffer -> {
                    int size = dataBuffer.readableByteCount();
                    byte[] byteContent = new byte[size];
                    dataBuffer.read(byteContent);

                    BookContent bookContent = new BookContent();
                    bookContent.setContent(byteContent);
                    bookContent.setSize(size);
                    bookContent.setMediaType(getMediaTypeFromByteArray(byteContent));
                    log.info("BookContent was uploaded with size: {} and media type: {}", size, bookContent.getMediaType());
                    return bookContentRepository.save(bookContent);
                })
                .map(bookContentMapper::toBookContentDto
                )
                .onErrorMap(IOException.class, e -> new RuntimeException("Проблема с обработкой данных", e));
    }

    public static String getMediaTypeFromByteArray(byte[] byteArray) {
        Tika tika = new Tika();
        return tika.detect(byteArray);
    }

    public Mono<BookContent> findById(Long id) {
        return bookContentRepository.findById(id);
    }
}
