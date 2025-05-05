package ru.korevg.bookreactapp.mapper;

import org.mapstruct.Mapper;
import ru.korevg.bookreactapp.domain.BookContent;
import ru.korevg.bookreactapp.dto.BookContentDto;

@Mapper
public interface BookContentMapper {

    BookContentDto toBookContentDto(BookContent bookContent);
}
