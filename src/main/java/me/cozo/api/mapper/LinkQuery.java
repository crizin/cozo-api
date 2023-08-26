package me.cozo.api.mapper;

import lombok.RequiredArgsConstructor;
import me.cozo.api.domain.dto.LinkDto;
import me.cozo.api.domain.dto.PageDto;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.LinkRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LinkQuery {

	private static final int PAGE_SIZE = 20;

	private final ArticleRepository articleRepository;
	private final LinkRepository linkRepository;

	@Cacheable(cacheNames = "links", cacheManager = "oneMinuteCacheManager")
	public PageDto<List<LinkDto>, Integer> getLinks(int page) {
		var links = linkRepository.findAll(PageRequest.of(page - 1, PAGE_SIZE, Sort.Direction.DESC, "lastUsedAt")).stream()
			.map(link -> LinkDto.of(link, articleRepository.findTop5ByLinksContainsOrderByCreatedAtDesc(link)))
			.toList();

		int lastPage = linkRepository.findMaxId()
			.map(id -> id.doubleValue() / PAGE_SIZE)
			.map(Math::ceil)
			.map(Double::intValue)
			.orElse(1);

		return new PageDto<>(
			links,
			page > 1 ? page - 1 : null,
			page < lastPage ? page + 1 : null
		);
	}
}
