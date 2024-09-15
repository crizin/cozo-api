package me.cozo.api.domain.helper;

import lombok.Getter;
import me.cozo.api.application.crawler.InvalidLinkException;
import me.cozo.api.domain.model.Link;
import me.cozo.api.domain.model.Link.Type;
import me.cozo.api.infrastructure.helper.TextUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Getter
public enum LinkBuilder {
	TYPICAL_IMAGE(
		Type.IMAGE,
		"(.*\\.(?:jpe?g|gif|png|bmp|mp4|webp|img)(?:\\?.*|$))",
		"%s"
	),
	TYPICAL_VIDEO(
		Type.VIDEO,
		"(.*\\.(?:webm)(?:\\?.*|$))",
		"%s"
	),
	HOSTING(
		Type.IMAGE,
		"(https?://(?:img\\.theqoo\\.net|gfycat\\.com|\\w+\\.daumcdn\\.net|pbs\\.twimg\\.com|i\\d+\\.ruliweb\\.com|imagedelivery\\.net)/[^&?]+)",
		"%s"
	),
	YOUTUBE_1(
		Type.VIDEO,
		"(?:https?:)?//(?:\\w+\\.)?youtube(?:-nocookie)?\\.com/(?:watch\\?(?:.*?)\\bv=|embed/|v/|shorts/)([^&?]+)",
		"https://www.youtube.com/watch?v=%s"
	),
	YOUTUBE_2(
		Type.VIDEO,
		"(?:https?:)?//(?:\\w+\\.|[a-z]{1,3})?youtu\\.be/([^&?]+)",
		"https://www.youtube.com/watch?v=%s"
	),
	NAVER_TV(
		Type.VIDEO,
		"(?:https?:)?//tv\\.naver\\.com/embed/(\\d+)",
		"https://tv.naver.com/v/%s"
	),
	TWITCH(
		Type.VIDEO,
		"https?://clips\\.twitch\\.tv/embed\\?.*clip=([^&?]+)",
		"https://clips.twitch.tv/%s"
	),
	STREAMABLE(
		Type.VIDEO,
		"https?://streamable.com/e/([^&?]+)",
		"https://streamable.com/%s"
	),
	INSTAGRAM1(
		Type.WEB,
		"https?://(?:www\\.)?instagram\\.com/([^?/]{2,})",
		"https://www.instagram.com/%s/"
	),
	INSTAGRAM2(
		Type.WEB,
		"https?://(?:www\\.)?instagram\\.com/p/([^?/]+)",
		"https://www.instagram.com/p/%s/"
	);

	private static final Pattern PATTERN_URL = Pattern.compile("^https?://");

	private final Type type;
	private final Pattern pattern;
	private final String template;

	LinkBuilder(Type type, String pattern, String template) {
		this.type = type;
		this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		this.template = template;
	}

	public static Link build(String url) {
		if (!PATTERN_URL.matcher(StringUtils.defaultString(url)).find() || !url.contains(".")) {
			throw new InvalidLinkException("Invalid URL pattern [url=%s]".formatted(url));
		}

		for (LinkBuilder linkBuilder : values()) {
			var matcher = linkBuilder.pattern.matcher(url);

			if (matcher.find()) {
				var normalizedUrl = linkBuilder.template.formatted(matcher.group(1));

				return Link.builder()
					.type(linkBuilder.type)
					.host(TextUtils.extractHost(normalizedUrl))
					.url(normalizedUrl)
					.lastUsedAt(LocalDateTime.now())
					.build();
			}
		}

		return Link.builder()
			.type(Type.WEB)
			.host(TextUtils.extractHost(url))
			.url(url)
			.lastUsedAt(LocalDateTime.now())
			.build();
	}

}
