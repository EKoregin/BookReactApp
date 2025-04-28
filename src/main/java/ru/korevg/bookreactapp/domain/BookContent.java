package ru.korevg.bookreactapp.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("content")
public class BookContent {

    @Id
    public Long id;
    public String mediaType;
    public Integer size;
    public String content;
}
