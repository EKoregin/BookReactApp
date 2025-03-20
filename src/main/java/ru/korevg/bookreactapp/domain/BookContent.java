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
    public @NonNull String mediaType;
    public @NonNull Integer size;
    public byte[] content;
}
