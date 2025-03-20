package ru.korevg.bookreactapp.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.korevg.bookreactapp.domain.BookContent;

public interface BookContentRepository extends ReactiveCrudRepository<BookContent, Long> {
}
