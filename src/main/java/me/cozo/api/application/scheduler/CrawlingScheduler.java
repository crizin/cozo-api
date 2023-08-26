package me.cozo.api.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.FetchBoardCommand;
import me.cozo.api.config.CommandGateway;
import me.cozo.api.domain.model.Board;
import me.cozo.api.domain.repository.BoardRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingScheduler {

	private final CommandGateway commandGateway;
	private final BoardRepository boardRepository;

	@Scheduled(cron = "0 * * * * *")
	@SchedulerLock(name = "CrawlingScheduler.run")
	public void run() {
		boardRepository.findAllByActiveIsTrueOrderBySiteName()
			.forEach(this::fetchBoard);
	}

	public void fetchBoard(Board board) {
		commandGateway.send(new FetchBoardCommand(board.getId()));
	}
}
