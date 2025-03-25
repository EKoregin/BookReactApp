package ru.korevg.bookreactapp.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.korevg.bookreactapp.dto.BookSearchDto;

@Service
@RequiredArgsConstructor
public class ElasticSearchService {

    private final ReactiveElasticsearchTemplate elasticsearchTemplate;

    public Flux<SearchHit<BookSearchDto>> searchBooks(String search) {
        Criteria criteria = new Criteria("title").is(search)
                .or("author").is(search)
                .or("isbn").is(search);
        Query searchQuery = new CriteriaQuery(criteria);

        return elasticsearchTemplate.search(searchQuery, BookSearchDto.class);
    }
}
