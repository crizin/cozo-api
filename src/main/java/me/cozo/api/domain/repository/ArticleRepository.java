package me.cozo.api.domain.repository;

import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Board;
import me.cozo.api.domain.model.Link;
import me.cozo.api.domain.model.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

	Optional<Article> findByBoardAndOriginId(Board board, String originId);

	Optional<Article> findTopByBoardOrderByCreatedAtDesc(Board board);

	List<Article> findAllBy(Pageable pageable);

	List<Article> findAllByIdLessThan(Long cursor, Pageable pageable);

	List<Article> findAllByIdGreaterThan(Long cursor, Pageable pageable);

	List<Article> findAllByBoard(Board board, Pageable pageable);

	List<Article> findAllByBoardAndIdLessThan(Board board, Long cursor, Pageable pageable);

	List<Article> findAllByBoardAndIdGreaterThan(Board board, Long cursor, Pageable pageable);

	List<Article> findAllByCreatedAtBetween(LocalDateTime begin, LocalDateTime end, Pageable pageable);

	List<Article> findAllByCreatedAtLessThan(LocalDateTime dateTime, Pageable pageable);

	List<Article> findAllByTagsContainsOrderByCreatedAtDesc(Tag tag, Pageable pageable);

	List<Article> findAllByTagsContainsAndCreatedAtBetweenOrderByClicksDescCreatedAtDesc(Tag tag, LocalDateTime begin, LocalDateTime end, Pageable pageable);

	List<Article> findAllByVectorIsNullAndCreatedAtBefore(Pageable pageable, LocalDateTime dateTime);

	List<Article> findTop5ByLinksContainsOrderByCreatedAtDesc(Link link);
}
