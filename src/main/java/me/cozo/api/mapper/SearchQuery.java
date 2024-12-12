package me.cozo.api.mapper;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import lombok.RequiredArgsConstructor;
import me.cozo.api.domain.dto.ArticleDto;
import me.cozo.api.domain.dto.PageDto;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Board;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.BoardRepository;
import me.cozo.api.domain.repository.search.SearchRepository;
import me.cozo.api.domain.search.ArticleDocument;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchQuery {

	private static final Set<String> STOP_QUERIES = Set.of("AND", "OR", "NOT");
	private static final Pattern PATTERN_ELASTICSEARCH_ESCAPE_CHARACTERS = Pattern.compile("[+\\-=&|><!(){}\\[\\]^\"~*?:\\\\/]");
	private static final HighlightQuery HIGHLIGHT_QUERY = new HighlightQuery(new Highlight(List.of(
		new HighlightField("title", HighlightFieldParameters.builder().withPreTags("@@HL@@").withPostTags("@@/HL@@").build()),
		new HighlightField("content", HighlightFieldParameters.builder().withPreTags("@@HL@@").withPostTags("@@/HL@@").build())
	)), ArticleDocument.class);

	private final ElasticsearchOperations operations;
	private final BoardRepository boardRepository;
	private final ArticleRepository articleRepository;
	private final SearchRepository searchRepository;

	public PageDto<SearchResult, Integer> search(String keyword, int page, Long boardId) {
		var query = NativeQuery.builder()
			.withQuery(QueryBuilders.bool(bool -> {
				bool.must(must -> must.queryString(queryString ->
						queryString.fields("title", "content")
							.defaultOperator(Operator.And)
							.query(escape(keyword))
					)
				);

				if (boardId != null) {
					bool.must(must -> must.term(t -> t.field("boardId").value(v -> v.longValue(boardId))));
				}

				return bool;
			}))
			.withFields("title", "content")
			.withPageable(PageRequest.of(page - 1, 20))
			.withSort(Sort.by("createdAt").descending())
			.withHighlightQuery(HIGHLIGHT_QUERY)
			.withAggregation("countByBoard", Aggregation.of(agg -> agg.terms(term -> term.field("boardId"))))
			.build();

		var response = operations.search(query, ArticleDocument.class);

		var articleIds = response.get()
			.map(SearchHit::getContent)
			.map(ArticleDocument::getId)
			.distinct()
			.toList();

		var titles = response.get()
			.collect(Collectors.toMap(
				hit -> hit.getContent().getId(),
				hit -> {
					var highlightFields = hit.getHighlightFields();

					if (highlightFields.containsKey("title")) {
						return String.join(" … ", highlightFields.get("title"));
					} else {
						return hit.getContent().getTitle();
					}
				}
			));

		var contents = response.get()
			.collect(Collectors.toMap(
				hit -> hit.getContent().getId(),
				hit -> {
					var highlightFields = hit.getHighlightFields();

					if (highlightFields.containsKey("content")) {
						return String.join(" … ", highlightFields.get("content"));
					} else {
						return hit.getContent().getContent();
					}
				}
			));

		var articles = articleRepository.findAllById(articleIds).stream().collect(Collectors.toMap(Article::getId, Function.identity()));

		var totalHits = new AtomicLong(response.getTotalHits());

		articleIds.stream().filter(id -> !articles.containsKey(id)).forEach(id -> {
			searchRepository.deleteById(id);
			totalHits.decrementAndGet();
		});

		var buckets = ((ElasticsearchAggregations) Objects.requireNonNull(response.getAggregations()))
			.aggregationsAsMap().get("countByBoard").aggregation().getAggregate()
			.lterms().buckets().array();

		var boards = buckets.stream()
			.map(LongTermsBucket::key)
			.collect(Collectors.collectingAndThen(Collectors.toSet(), boardRepository::findAllById))
			.stream()
			.collect(Collectors.toMap(Board::getId, Function.identity()));

		var boardCounts = buckets.stream()
			.filter(bucket -> boards.containsKey(bucket.key()))
			.sorted(Comparator.comparing(LongTermsBucket::docCount).reversed())
			.collect(Collectors.toMap(
				bucket -> boards.get(bucket.key()).getId(),
				LongTermsBucket::docCount,
				(a, b) -> a,
				LinkedHashMap::new
			));

		var result = new SearchResult(
			keyword,
			totalHits.longValue(),
			articleIds.stream().map(articles::get).filter(Objects::nonNull).map(ArticleDto::of).toList(),
			titles,
			contents,
			boardCounts
		);

		var prevPage = page > 1 ? page - 1 : null;
		var nextPage = page < Math.ceil(result.totalHits() / 20d) ? page + 1 : null;

		return new PageDto<>(result, prevPage, nextPage);
	}

	private String escape(String keyword) {
		String query = PATTERN_ELASTICSEARCH_ESCAPE_CHARACTERS.matcher(keyword).replaceAll(" ");

		return Arrays.stream(query.split("\\s+"))
			.filter(StringUtils::isNotBlank)
			.filter(word -> !STOP_QUERIES.contains(word))
			.distinct()
			.collect(Collectors.joining(" "));
	}

	public record SearchResult(
		String keyword,
		long totalHits,
		List<ArticleDto> articles,
		Map<Long, String> titles,
		Map<Long, String> contents,
		Map<Long, Long> boardCounts
	) {
	}
}
