package me.cozo.api.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.domain.search.ArticleDocument;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SearchClient {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Pattern PATTERN_ACCEPT_WORDS = Pattern.compile("^[A-Z0-9ㄱ-힣]+$");
	private static final Set<String> STOP_WORDS = Arrays.stream("""
		your browser does support the video tag
		http https www co kr com net youtu be jpg gif img php txt html
		feat vs manhwa no mnews watch sec status board main mid mode oid gfycat read view sid article
		news me aid id entertain idxno bbs shm and or not
		"""
		.toUpperCase().split("\\s+")).collect(Collectors.toSet());

	private final SearchClientExchanger searchClientExchanger;

	public SearchClient(@Value("${spring.elasticsearch.uris}") String uris) {
		WebClient webClient = WebClient.builder()
			.baseUrl(uris)
			.exchangeStrategies(ExchangeStrategies.builder()
				.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
				.build()
			)
			.build();

		this.searchClientExchanger = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build()
			.createClient(SearchClientExchanger.class);
	}

	@SneakyThrows
	public Set<String> analyze(String... strings) {
		List<String> texts = Arrays.stream(strings)
			.filter(StringUtils::isNotBlank)
			.toList();

		if (texts.isEmpty()) {
			return Collections.emptySet();
		}

		var request = """
			{
			    "analyzer": "%s",
			    "explain": true,
			    "attributes": ["leftPOS"],
			    "text": %s
			}
			""".formatted(ArticleDocument.ANALYZER, OBJECT_MAPPER.writeValueAsString(texts));

		JsonNode response;

		try {
			response = searchClientExchanger.analyze(ArticleDocument.INDEX, request);
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
			return Collections.emptySet();
		}

		var tokens = new ArrayList<JsonNode>();

		for (JsonNode token : response.get("detail").get("tokenizer").get("tokens")) {
			tokens.add(token);
		}

		for (JsonNode filters : response.get("detail").get("tokenfilters")) {
			for (JsonNode token : filters.get("tokens")) {
				tokens.add(token);
			}
		}

		return tokens.stream()
			.filter(this::acceptToken)
			.map(token -> token.get("token").asText().toUpperCase().strip())
			.collect(Collectors.toSet());
	}

	private boolean acceptToken(JsonNode token) {
		var type = token.path("type").asText(null);
		var pos = token.path("leftPOS").asText(null);
		var word = StringUtils.deleteWhitespace(token.get("token").asText()).toUpperCase();

		if (StringUtils.equals(type, "SYNONYM")) {
			return false;
		}

		if (!(StringUtils.startsWith(pos, "N") || StringUtils.startsWithAny(pos, "SL", "SH"))) {
			return false;
		}

		if (StringUtils.startsWithAny(pos, "NNB", "NR", "NP")) {
			return false;
		}

		if (word.length() > 30) {
			return false;
		}

		if (!PATTERN_ACCEPT_WORDS.matcher(word).matches()) {
			return false;
		}

		if (STOP_WORDS.contains(word)) {
			return false;
		}

		return word.startsWith("S") ? word.length() > 2 : word.length() > 1;
	}
}
