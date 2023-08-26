package me.cozo.api.domain.repository;

import me.cozo.api.domain.model.TagTrend;
import me.cozo.api.domain.model.TagTrendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TagTrendRepository extends JpaRepository<TagTrend, TagTrendId> {

	@Query("SELECT MAX(tt.id.date) FROM TagTrend tt")
	Optional<LocalDate> findLatestTagTrendDate();

	Optional<TagTrend> findTopByIdDateLessThanOrderByIdDateDesc(LocalDate date);

	Optional<TagTrend> findTopByIdDateGreaterThanOrderByIdDate(LocalDate date);

	List<TagTrend> findAllByIdDateOrderByIdRanking(LocalDate date);

	int deleteByIdDate(LocalDate date);
}
