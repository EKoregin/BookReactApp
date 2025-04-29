package ru.korevg.bookreactapp.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = "id")
@ToString
@Table("book")
public class Book {

    @Id
    private Long id;
    private @NonNull String title;
    private @NonNull String author;
    private @NonNull String isbn;
    private @NonNull BigDecimal price;
    private String content;
}
