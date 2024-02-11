package me.cozo.api.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class OpenAiClient {

	private final boolean enabled;
	private final OpenAiExchanger openAiExchanger;

	public OpenAiClient(OpenAiProperty property, ObjectMapper objectMapper) {
		this.enabled = StringUtils.isNotBlank(property.getKey());

		ObjectMapper snakeMapper = objectMapper.copy();
		snakeMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

		this.openAiExchanger = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(WebClient.builder()
				.exchangeStrategies(ExchangeStrategies.builder()
					.codecs(clientCodecConfigurer -> {
						clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(snakeMapper));
						clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(snakeMapper));
					})
					.build()
				)
				//.clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)))
				.baseUrl(property.getUrl())
				.defaultHeader("Authorization", String.format("Bearer %s", property.getKey()))
				.build()
			))
			.build()
			.createClient(OpenAiExchanger.class);
	}

	public List<Double> embedding(String content) {
		if (!enabled || StringUtils.isBlank(content)) {
			return Collections.emptyList();
		}

		EmbeddingResponse response;

		try {
			response = openAiExchanger.embeddings(new EmbeddingRequest("text-embedding-3-small", content));
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
			return Collections.emptyList();
		}

		return response.data().stream().findFirst().map(EmbeddingResponse.Data::embedding).orElse(List.of());
	}

	record EmbeddingRequest(String model, String input) {
	}

	record EmbeddingResponse(List<Data> data) {

		record Data(List<Double> embedding) {
		}
	}
}
