package me.cozo.api.domain.repository;

import me.cozo.api.domain.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

	List<Board> findAllByOrderBySiteName();

	List<Board> findAllByActiveIsTrueOrderBySiteName();
}
