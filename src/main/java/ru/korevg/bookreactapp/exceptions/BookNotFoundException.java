package ru.korevg.bookreactapp.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
