package ru.korevg.bookreactapp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookDTO {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
}
