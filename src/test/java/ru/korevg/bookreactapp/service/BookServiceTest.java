package ru.korevg.bookreactapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.korevg.bookreactapp.domain.Book;
import ru.korevg.bookreactapp.dto.BookDTO;
import ru.korevg.bookreactapp.exceptions.BookNotFoundException;
import ru.korevg.bookreactapp.mapper.BookMapper;
import ru.korevg.bookreactapp.producer.BookIndexProducer;
import ru.korevg.bookreactapp.repository.BookContentRepository;
import ru.korevg.bookreactapp.repository.BookRepository;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final String TEST_ISBN = "123456789";

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookIndexProducer bookIndexProducer;
    @Mock
    private BookContentRepository bookContentRepository;

    BookDTO bookDTO = new BookDTO();
    Book book = new Book();
    Book savedBook = new Book();
    BookDTO savedBookDTO = new BookDTO();

    @BeforeEach
    public void setUp() {
        bookDTO.setIsbn(TEST_ISBN);
        book.setIsbn(TEST_ISBN);
        savedBook.setIsbn(TEST_ISBN);
        savedBook.setId(1L);
        savedBookDTO.setIsbn(TEST_ISBN);
        savedBookDTO.setId(1L);
    }

    @Test
    @DisplayName("Поиск книги по isbn. Успешно")
    public void findBookOK() {

        Book book = new Book();
        BookDTO bookDTO = new BookDTO();

        when(bookRepository.findByIsbn(TEST_ISBN)).thenReturn(Mono.just(book));
        when(bookMapper.toBookDTO(book)).thenReturn(bookDTO);

        StepVerifier.create(bookService.findByIsbn(TEST_ISBN))
                .expectNext(bookDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName(("Поиск книги по isbn. Книга не найдена"))
    public void findBookNotFound() {
        when(bookRepository.findByIsbn(TEST_ISBN)).thenReturn(Mono.empty());

        StepVerifier.create(bookService.findByIsbn(TEST_ISBN))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("Создание книги. Успешно")
    public void createBookOK() {
        when(bookMapper.toBook(bookDTO)).thenReturn(book);
        when(bookMapper.toBookDTO(savedBook)).thenReturn(savedBookDTO);
        when(bookRepository.save(book)).thenReturn(Mono.just(savedBook));

        StepVerifier.create(bookService.create(bookDTO))
                .expectNext(savedBookDTO)
                .verifyComplete();

        String expectedMessage = String.format("Book with isbn: {%s} need reindex in ElasticSearch", savedBook.getIsbn());
        verify(bookIndexProducer).sendMessage(expectedMessage);
    }

    @Test
    @DisplayName("Обновление книги. Книга не найдена")
    public void updateBookNotFound() {
        when(bookRepository.findByIsbn(TEST_ISBN)).thenReturn(Mono.empty());

        StepVerifier.create(bookService.update(TEST_ISBN, bookDTO))
                .verifyError(BookNotFoundException.class);
    }

    @Test
    @DisplayName("Обновление книги. Успешно")
    public void updateBookOK() {
        when(bookRepository.findByIsbn(TEST_ISBN)).thenReturn(Mono.just(book));
        when(bookMapper.toBook(bookDTO)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(Mono.just(savedBook));
        when(bookMapper.toBookDTO(savedBook)).thenReturn(savedBookDTO);

        StepVerifier.create(bookService.update(TEST_ISBN, bookDTO))
                .expectNext(savedBookDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Удаление книги без контента. Успешно")
    public void deleteBookOK() {
        when(bookRepository.findByIsbn(TEST_ISBN)).thenReturn(Mono.just(savedBook));
        when(bookRepository.delete(savedBook)).thenReturn(Mono.empty());

        StepVerifier.create(bookService.delete(TEST_ISBN)).verifyComplete();
        verify(bookRepository).delete(savedBook);
        verify(bookRepository).findByIsbn(TEST_ISBN);
    }

    @Disabled
    @Test
    @DisplayName("Удаление книги с контентом. Успешно")
    public void deleteBookWithContent() {
        savedBook.setContent("key");

        when(bookRepository.findByIsbn(TEST_ISBN)).thenReturn(Mono.just(savedBook));
        when(bookRepository.delete(savedBook)).thenReturn(Mono.empty());
        when(bookContentRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(bookService.delete(TEST_ISBN)).verifyComplete();
        verify(bookRepository).delete(savedBook);
        verify(bookRepository).findByIsbn(TEST_ISBN);
        verify(bookContentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Удаление книги. Книга не найдена")
    public void deleteBookNotFound() {
        when(bookRepository.findByIsbn(TEST_ISBN)).thenReturn(Mono.empty());
        StepVerifier.create(bookService.delete(TEST_ISBN))
                .verifyError(BookNotFoundException.class);

        verify(bookRepository).findByIsbn(TEST_ISBN);
        verify(bookContentRepository, never()).deleteById(Mockito.anyLong());
        verify(bookRepository, never()).delete(Mockito.any());
    }


}