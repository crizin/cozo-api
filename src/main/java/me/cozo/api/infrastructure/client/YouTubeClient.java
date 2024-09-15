package me.cozo.api.infrastructure.client;

import me.cozo.api.domain.helper.LinkBuilder;
import me.cozo.api.domain.model.Link;
import net.crizin.webs.Webs;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Stream;

@Component
public class YouTubeClient {

	private final String apiKey;
	private final Webs webs = Webs.builder().baseUrl("https://www.googleapis.com").build();

	public YouTubeClient(@Value("${cozo.youtube.api-key}") String apiKey) {
		this.apiKey = apiKey;
	}

	public boolean isYoutubeVideo(Link link) {
		if (StringUtils.isBlank(apiKey)) {
			return false;
		}

		return Stream.of(LinkBuilder.YOUTUBE_1, LinkBuilder.YOUTUBE_2)
			.map(builder -> builder.getPattern().matcher(link.getUrl()))
			.anyMatch(Matcher::find);
	}

	public void fetchVideo(Link link) {
		if (StringUtils.isBlank(apiKey)) {
			throw new IllegalStateException("Youtube API key not found");
		}

		var videoId = Stream.of(LinkBuilder.YOUTUBE_1, LinkBuilder.YOUTUBE_2)
			.map(builder -> builder.getPattern().matcher(link.getUrl()))
			.filter(Matcher::find)
			.findFirst()
			.map(matcher -> matcher.group(1))
			.orElse(null);

		if (videoId == null) {
			throw new IllegalArgumentException("Invalid Youtube link [link=%s]".formatted(link.getUrl()));
		}

		var videos = webs.get("/youtube/v3/videos")
			.queryParam("part", "snippet")
			.queryParam("id", videoId)
			.queryParam("key", apiKey)
			.fetchAs(Videos.class);

		if (CollectionUtils.isEmpty(videos.items)) {
			throw new IllegalArgumentException("Video not found [videoId=%s]".formatted(videoId));
		}

		var video = videos.items.getFirst();
		var snippet = video.snippet();

		link.updateUrl("https://www.youtube.com/watch?v=" + videoId);
		link.updateTitle(snippet.title());
		link.updateDescription(snippet.description());
		link.updateThumbnailUrl(snippet.getLargestThumbnail());
		link.updateFaviconUrl("https://m.youtube.com/static/favicon.ico");
	}

	private record Videos(List<Video> items) {

		private record Video(String id, Snippet snippet) {

			private record Snippet(String title, String description, Map<String, Thumbnail> thumbnails) {

				private record Thumbnail(String url, int width, int height) {
				}

				public String getLargestThumbnail() {
					return thumbnails.entrySet().stream()
						.max(Comparator.comparingInt(entry -> entry.getValue().width() * entry.getValue().height()))
						.map(Map.Entry::getValue)
						.map(Thumbnail::url)
						.orElse(null);
				}
			}
		}
	}
}
