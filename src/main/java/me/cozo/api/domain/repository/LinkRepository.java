package me.cozo.api.domain.repository;

import me.cozo.api.domain.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {

	Optional<Link> findByUrl(String url);

	@Query("SELECT MAX(l.id) FROM Link l")
	Optional<Long> findMaxId();
}
