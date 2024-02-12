package me.cozo.api.mapper;

import lombok.RequiredArgsConstructor;
import me.cozo.api.domain.dto.ArticleDto;
import me.cozo.api.domain.dto.PageDto;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Board;
import me.cozo.api.domain.repository.ArticleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ArticleQuery {

	private static final int PAGE_SIZE = 40;

	private final ArticleRepository articleRepository;

	@Cacheable(cacheNames = "articles", cacheManager = "oneMinuteCacheManager", key = "{#prevCursor, #nextCursor}")
	public PageDto<List<ArticleDto>, Long> getArticles(Long prevCursor, Long nextCursor) {
		return getArticles(
			prevCursor, nextCursor,
			() -> articleRepository.findAllBy(PageRequest.of(0, PAGE_SIZE + 1, Direction.DESC, "id")),
			() -> articleRepository.findAllByIdLessThan(nextCursor, PageRequest.of(0, PAGE_SIZE + 1, Direction.DESC, "id")),
			() -> articleRepository.findAllByIdGreaterThan(prevCursor, PageRequest.of(0, PAGE_SIZE + 1, Direction.ASC, "id")),
			articles -> !articleRepository.findAllByIdLessThan(articles.getLast().getId(), PageRequest.of(0, 1, Direction.DESC, "id")).isEmpty(),
			articles -> !articleRepository.findAllByIdGreaterThan(articles.getFirst().getId(), PageRequest.of(0, 1, Direction.ASC, "id")).isEmpty()
		);
	}

	@Cacheable(cacheNames = "articlesByBoard", cacheManager = "oneMinuteCacheManager", key = "{#board.id, #prevCursor, #nextCursor}")
	public PageDto<List<ArticleDto>, Long> getArticles(Board board, Long prevCursor, Long nextCursor) {
		return getArticles(
			prevCursor, nextCursor,
			() -> articleRepository.findAllByBoard(board, PageRequest.of(0, PAGE_SIZE + 1, Direction.DESC, "id")),
			() -> articleRepository.findAllByBoardAndIdLessThan(board, nextCursor, PageRequest.of(0, PAGE_SIZE + 1, Direction.DESC, "id")),
			() -> articleRepository.findAllByBoardAndIdGreaterThan(board, prevCursor, PageRequest.of(0, PAGE_SIZE + 1, Direction.ASC, "id")),
			articles -> !articleRepository.findAllByBoardAndIdLessThan(board, articles.getLast().getId(), PageRequest.of(0, 1, Direction.DESC, "id")).isEmpty(),
			articles -> !articleRepository.findAllByBoardAndIdGreaterThan(board, articles.getFirst().getId(), PageRequest.of(0, 1, Direction.ASC, "id")).isEmpty()
		);
	}

	private PageDto<List<ArticleDto>, Long> getArticles(
		Long prevCursor, Long nextCursor,
		Supplier<List<Article>> firstPageSupplier, Supplier<List<Article>> nextPageSupplier, Supplier<List<Article>> prevPageSupplier,
		Predicate<List<Article>> hasNextPageSupplier, Predicate<List<Article>> hasPrevPageSupplier
	) {
		if (prevCursor != null) {
			return getOlderArticles(prevPageSupplier, hasNextPageSupplier);
		} else {
			return getNewerArticles(nextCursor, firstPageSupplier, nextPageSupplier, hasPrevPageSupplier);
		}
	}

	public PageDto<List<ArticleDto>, Long> getOlderArticles(Supplier<List<Article>> prevPageSupplier, Predicate<List<Article>> hasNextPageSupplier) {
		Long newPrevPageCursor = null;

		List<Article> articles = prevPageSupplier.get();
		Collections.reverse(articles);

		if (articles.size() < PAGE_SIZE) {
			return getArticles(null, null);
		}

		if (articles.size() == PAGE_SIZE + 1) {
			articles.removeFirst();
			newPrevPageCursor = articles.getFirst().getId() * -1;
		}

		Long newNextPageCursor = hasNextPageSupplier.test(articles) ? articles.getLast().getId() : null;

		return new PageDto<>(articles.stream().map(ArticleDto::of).toList(), newPrevPageCursor, newNextPageCursor);
	}

	public PageDto<List<ArticleDto>, Long> getNewerArticles(
		Long nextCursor, Supplier<List<Article>> firstPageSupplier, Supplier<List<Article>> nextPageSupplier, Predicate<List<Article>> hasPrevPageSupplier
	) {
		Long newPrevPageCursor = null;
		Long newNextPageCursor = null;

		List<Article> articles = (nextCursor == null) ? firstPageSupplier.get() : nextPageSupplier.get();

		if (articles.size() == PAGE_SIZE + 1) {
			articles.removeLast();
			newNextPageCursor = articles.getLast().getId();
		}

		if (!articles.isEmpty()) {
			newPrevPageCursor = hasPrevPageSupplier.test(articles) ? articles.getFirst().getId() * -1 : null;
		}

		return new PageDto<>(articles.stream().map(ArticleDto::of).toList(), newPrevPageCursor, newNextPageCursor);
	}
}
