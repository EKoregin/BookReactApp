package ru.korevg.bookreactapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.korevg.bookreactapp.domain.Book;
import ru.korevg.bookreactapp.dto.BookDTO;
import ru.korevg.bookreactapp.exceptions.BookNotFoundException;
import ru.korevg.bookreactapp.mapper.BookMapper;
import ru.korevg.bookreactapp.repository.BookRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public Flux<BookDTO> findAll() {
        return bookRepository.findAll()
                .map(bookMapper::toBookDTO);
    }

    public Mono<BookDTO> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(bookMapper::toBookDTO);
    }

    @Transactional
    public Mono<BookDTO> create(BookDTO dto) {
        return bookRepository.save(bookMapper.toBook(dto))
                .map(bookMapper::toBookDTO);
    }

    @Transactional
    public Mono<BookDTO> update(String isbn, BookDTO dto) {
        return bookRepository.findByIsbn(isbn)
                .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with isbn " + isbn)))
                .flatMap(book -> {
                    log.info("Update book with isbn {}", book.getIsbn());
                    Book updatedBook = bookMapper.toBook(dto);
                    updatedBook.setId(book.getId());
                    return bookRepository.save(updatedBook);
                })
                .map(bookMapper::toBookDTO)
                .onErrorResume(Mono::error);
    }

    public Mono<Void> delete(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with isbn " + isbn)))
                .flatMap(book -> {
                    log.info("Deleting book with isbn {}", book.getIsbn());
                    return bookRepository.delete(book);
                })
                .then();
    }
}
