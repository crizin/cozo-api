package me.cozo.api.domain.repository;

import me.cozo.api.domain.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {

	Optional<Tag> findByName(String name);

	@Query(nativeQuery = true, value = """
		SELECT at.tag_id AS tagId, t.name AS tagName
		FROM article AS a
		    JOIN article_tag AS at ON a.id = at.article_id
		    JOIN tag AS t ON t.id = at.tag_id
		WHERE a.created_at BETWEEN :begin AND :end
		GROUP BY at.tag_id
		ORDER BY COUNT(*) DESC
		LIMIT :limit
		""")
	List<Map<String, Object>> countByTags(LocalDateTime begin, LocalDateTime end, int limit);

	Set<Tag> findAllByNameIn(Collection<String> names);
}
