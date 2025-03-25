package ru.korevg.bookreactapp.dto;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;

@Data
@Document(indexName = "booksearchdto")
public class BookSearchDto {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
}
