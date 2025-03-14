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

    public Flux<Book> findAll() {
        return bookRepository.findAll()
                .doOnNext(book -> {
                    log.info("Getting book with isbn {}", book.getIsbn());
                });
    }

    public Mono<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Transactional
    public Mono<Book> create(BookDTO dto) {
        return bookRepository.save(bookMapper.toBook(dto));
    }

    @Transactional
    public Mono<Book> update(String isbn, BookDTO dto) {
        return bookRepository.findByIsbn(isbn)
                .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with isbn " + isbn)))
                .flatMap(book -> {
                    log.info("Update book with isbn {}", book.getIsbn());
                    Book updatedBook = bookMapper.toBook(dto);
                    updatedBook.setId(book.getId());
                    return bookRepository.save(updatedBook);
                })
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
