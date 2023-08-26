package me.cozo.api.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.BuildTagTrendCommand;
import me.cozo.api.config.CommandGateway;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class TagTrendBuilderScheduler {

	private final CommandGateway commandGateway;

	@Scheduled(cron = "0 * * * * *")
	@SchedulerLock(name = "TagTrendBuilderScheduler.run")
	public void run() {
		commandGateway.send(new BuildTagTrendCommand(LocalDate.now()));
	}

	@Scheduled(cron = "10 0 * * * *")
	@SchedulerLock(name = "TagTrendBuilderScheduler.runPrevHourly")
	public void runPrevHourly() {
		commandGateway.send(new BuildTagTrendCommand(LocalDate.now().minusDays(1)));
	}

	@Scheduled(cron = "30 0 0 * * *")
	@SchedulerLock(name = "TagTrendBuilderScheduler.runPrevDaily")
	public void runPrevDaily() {
		IntStream.rangeClosed(2, 7)
			.forEach(i -> commandGateway.send(new BuildTagTrendCommand(LocalDate.now().minusDays(i))));
	}
}
