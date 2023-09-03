package me.cozo.api.application.listener;

import lombok.RequiredArgsConstructor;
import me.cozo.api.domain.event.ArticleUpdatedEvent;
import me.cozo.api.domain.helper.LinkBuilder;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Link;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.LinkRepository;
import me.cozo.api.infrastructure.client.LinkClient;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LinkEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger("link");

	private static final Pattern[] PATTERNS_IGNORE = new Pattern[] {
		Pattern.compile("^https?://drive\\.google\\.com/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://[\\w-]+\\.googledrive\\.com/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://(player\\.)?bgmstore\\.net/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://market\\.android\\.com/details\\?id=com\\.alphafactory\\.", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://www\\.alphafactory\\.co\\.kr", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://nuridol\\.net/]", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://wowpc\\.net/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://creativecommons\\.org/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://nuridol\\.net/ut_convert.html", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://([^/]+\\.)?(zz|ze)\\.am/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://[\\w-]\\.clickmon\\.co\\.kr/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://s\\.ppomppu\\.co\\.kr", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://ygosu.com/community/board/download.yg", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://(\\w+\\.)?(twitter\\.com|t\\.co|facebook\\.com|instagram\\.com)/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("popall\\.net|link\\.naver\\.com|nid\\.naver\\.com|www\\.w3\\.org|notfound\\.html|email-protection|/ads/|ico_noimg_thumb", Pattern.CASE_INSENSITIVE),
	};

	private final LinkClient linkClient;
	private final ArticleRepository articleRepository;
	private final LinkRepository linkRepository;

	@Async("linkExecutor")
	@EventListener
	public void updateLink(ArticleUpdatedEvent event) {
		if (!event.linkUpdated()) {
			return;
		}

		var article = articleRepository.findById(event.articleId()).orElseThrow();
		var document = Jsoup.parse("<div>%s</div>".formatted(article.getContent()));

		var links = linkClient.collectLinks(document).stream()
			.map(url -> linkClient.resolveUrl(article.getPcUrl(), url))
			.map(url -> {
				try {
					return LinkBuilder.build(url);
				} catch (Exception e) {
					LOGGER.debug(e.getMessage(), e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.filter(link -> link.getHost() != null)
			.filter(link -> filterOutIgnored(link.getUrl()))
			.collect(Collectors.toSet());

		var containsImage = article.isContainsImage() || links.stream().map(Link::getType).anyMatch(type -> type == Link.Type.IMAGE);
		var containsVideo = article.isContainsVideo() || !document.select("video").isEmpty() || links.stream().map(Link::getType).anyMatch(type -> type == Link.Type.VIDEO);

		article.updateContains(containsImage, containsVideo);

		links = links.stream()
			.filter(link -> link.getType() != Link.Type.IMAGE)
			.collect(Collectors.toSet());

		if (links.isEmpty() || links.size() > 3) {
			return;
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Link found [size={}, url[0]={}]", links.size(), links.stream().findAny().map(Link::getUrl).orElse(null));
		}

		links = saveLinks(article, links);
		article.updateLinks(links);
		articleRepository.save(article);
	}

	private Set<Link> saveLinks(Article article, Set<Link> links) {
		Set<Link> updatedLinks;

		updatedLinks = links.stream()
			.map(link -> linkRepository.findByUrl(link.getUrl()).orElseGet(() -> {
				var originalUrl = link.getUrl();

				try {
					linkClient.fetchLink(link);
				} catch (Exception e) {
					LOGGER.error("Fetch Link - Failed [type={}, url={}, e={}]", link.getType(), link.getUrl(), e.getMessage());
					return null;
				}

				if (link.getUrl().equals(originalUrl)) {
					LOGGER.info("Fetch Link - Fetched [type={}, url={}]", link.getType(), link.getUrl());
				} else {
					LOGGER.info("Fetch Link - Fetched [type={}, url={} => {}]", link.getType(), originalUrl, link.getUrl());
				}

				return link;
			}))
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		return updatedLinks.stream()
			.filter(link -> link.getUrl().length() <= 768)
			.filter(link -> filterOutIgnored(link.getUrl()))
			.map(link -> linkRepository.findByUrl(link.getUrl()).orElseGet(() -> {
				linkRepository.save(link);
				return link;
			}))
			.map(link -> {
				link.updateLastUsedAt(article.getCreatedAt());
				return linkRepository.save(link);
			})
			.collect(Collectors.toSet());
	}

	private boolean filterOutIgnored(String url) {
		return Arrays.stream(PATTERNS_IGNORE)
			.map(pattern -> pattern.matcher(url))
			.noneMatch(Matcher::find);
	}
}
