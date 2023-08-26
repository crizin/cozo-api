package me.cozo.api.domain.repository.search;

import me.cozo.api.domain.search.ArticleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchRepository extends ElasticsearchRepository<ArticleDocument, Long> {
}
