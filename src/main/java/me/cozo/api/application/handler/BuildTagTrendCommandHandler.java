package me.cozo.api.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.BuildTagTrendCommand;
import me.cozo.api.application.command.UpdateTagTrendCommand;
import me.cozo.api.config.CommandGateway;
import me.cozo.api.domain.model.Tag;
import me.cozo.api.domain.model.TagTrend;
import me.cozo.api.domain.model.TagTrendId;
import me.cozo.api.domain.repository.TagRepository;
import me.cozo.api.domain.repository.TagTrendRepository;
import me.cozo.api.infrastructure.helper.DateUtils;
import me.cozo.api.mapper.TagQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildTagTrendCommandHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger("tag-trend-builder");

	private final CommandGateway commandGateway;
	private final TagQuery tagQuery;
	private final TagRepository tagRepository;
	private final TagTrendRepository tagTrendRepository;

	@ServiceActivator(inputChannel = "BuildTagTrendCommand")
	public void buildTagTrend(BuildTagTrendCommand command) {
		var target = command.date();

		LOGGER.info("{} - Start build tag trends", target);

		var lowerDateTime = target.minusDays(4).atStartOfDay();
		var upperDateTime = DateUtils.getEndOfDay(target.minusDays(2));
		var prevCountByTags = tagRepository.countByTags(lowerDateTime, upperDateTime, 1000);

		var prevRanks = IntStream.range(1, prevCountByTags.size() + 1)
			.boxed()
			.collect(Collectors.toMap(
				rank -> Count.of(prevCountByTags.get(rank - 1)).tagId,
				Function.identity()
			));

		if (target.isEqual(LocalDate.now())) {
			lowerDateTime = LocalDateTime.now().minusDays(1);
			upperDateTime = LocalDateTime.now();
		} else {
			lowerDateTime = target.atStartOfDay();
			upperDateTime = DateUtils.getEndOfDay(target);
		}

		var todayCountByTags = tagRepository.countByTags(lowerDateTime, upperDateTime, 1000).stream()
			.map(Count::of)
			.toList();

		if (todayCountByTags.isEmpty()) {
			LOGGER.info("{} - Empty tag trends", target);
			return;
		}

		var ignoreTagIds = new HashSet<Long>();

		for (Count count1 : todayCountByTags) {
			for (Count count2 : todayCountByTags) {
				if (count1.tagName.length() < count2.tagName.length() && count2.tagName.contains(count1.tagName)) {
					ignoreTagIds.add(count1.tagId);
					LOGGER.info("{} - Ignored tag '{}' ({} in {})", target, count1.tagName, count1.tagName, count2.tagName);
					break;
				}
			}
		}

		var counts = getAcceptTagmap(target, todayCountByTags, ignoreTagIds, prevRanks).entrySet().stream()
			.sorted(Map.Entry.<Count, Integer>comparingByValue().reversed())
			.map(Map.Entry::getKey)
			.toList();

		var tags = tagRepository.findAllById(counts.stream().map(c -> c.tagId).toList()).stream()
			.collect(Collectors.toMap(Tag::getId, Function.identity()));

		var tagTrends = IntStream.range(1, counts.size())
			.boxed()
			.filter(rank -> tags.containsKey(counts.get(rank - 1).tagId))
			.flatMap(rank -> Optional.ofNullable(tags.get(counts.get(rank - 1).tagId))
				.map(tag -> new TagTrend(new TagTrendId(target, rank), tag, rank))
				.stream()
			)
			.limit(30)
			.toList();

		commandGateway.send(new UpdateTagTrendCommand(target, tagTrends));
	}

	@Transactional
	@ServiceActivator(inputChannel = "UpdateTagTrendCommand")
	public void updateTagTrends(UpdateTagTrendCommand command) {
		LocalDate target = command.date();
		List<TagTrend> tagTrends = command.tagTrends();

		var rows = tagTrendRepository.deleteByIdDate(target);
		LOGGER.info("{} - {} rows are deleted", target, rows);

		tagTrendRepository.saveAll(tagTrends);
		LOGGER.info("{} - {} rows are inserted", target, tagTrends.size());

		tagQuery.refreshTagTrends(target);
	}

	private Map<Count, Integer> getAcceptTagmap(LocalDate target, List<Count> todayCountByTags, Set<Long> ignoreTagIds, Map<Long, Integer> prevRanks) {
		var ranking = 0;
		var acceptTagMap = new HashMap<Count, Integer>();

		for (Count count : todayCountByTags) {
			if (ignoreTagIds.contains(count.tagId)) {
				continue;
			}

			ranking++;

			var prevRanking = prevRanks.getOrDefault(count.tagId, 999_999);
			var diff = prevRanking - ranking;

			if (diff > 50) {
				acceptTagMap.put(count, diff);
				LOGGER.info("{} - Accept '{}' (ranking_up={})", target, count.tagName, diff);
				if (acceptTagMap.size() >= 500) {
					return acceptTagMap;
				}
			} else {
				LOGGER.info("{} - Denied '{}' (ranking_up={})", target, count.tagName, diff);
			}
		}

		return acceptTagMap;
	}

	private record Count(Long tagId, String tagName) {

		private static Count of(Map<String, Object> map) {
			return new Count((long) map.get("tagId"), (String) map.get("tagName"));
		}
	}
}
