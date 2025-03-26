package ru.korevg.bookreactapp.service;


import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.korevg.bookreactapp.dto.BookSearchDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticSearchService {

    private final ReactiveElasticsearchTemplate elasticsearchTemplate;

    /**
     * Ищет совпадение по полям.
     * Если указать несколько слов, то ищет их наличие в tokens
     */
    public Flux<BookSearchDto> searchBooks(String search) {
        Criteria criteria = new Criteria("title").is(search)
                .or("author").is(search)
                .or("isbn").is(search)
                .or("tokens").is(search);

        var searchQuery = new CriteriaQuery(criteria);
        searchQuery.setFields(List.of("title", "author", "isbn", "price"));

        return elasticsearchTemplate.search(searchQuery, BookSearchDto.class).map(SearchHit::getContent);
    }

    /**
     * Возвращает описания книги из ElasticSearch.
     * Часть полей может не выводится. Например можно отфильтровать content и tokens
     */
    public Flux<BookSearchDto> fullTextSearchForBookISBN(String searchPhrase) {
        log.info("Full text searching for phrase: \"{}\"", searchPhrase);

        MatchPhraseQuery matchPhraseQuery = QueryBuilders.matchPhrase()
                .field("content")
                .query(searchPhrase)
                .slop(2)
                .build();


        var query = new NativeQueryBuilder()
                .withQuery(matchPhraseQuery._toQuery())
                .withSourceFilter(new FetchSourceFilter(new String[0], new String[]{"content", "tokens"}))
                .withFields("id")
                .build();

        return elasticsearchTemplate.search(query, BookSearchDto.class).map(SearchHit::getContent);
    }
}
