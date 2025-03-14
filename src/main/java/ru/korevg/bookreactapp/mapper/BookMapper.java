package ru.korevg.bookreactapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.korevg.bookreactapp.domain.Book;
import ru.korevg.bookreactapp.dto.BookDTO;

@Mapper
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    Book toBook(BookDTO bookDTO);
}
