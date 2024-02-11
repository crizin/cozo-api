package me.cozo.api.infrastructure.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

interface OpenAiExchanger {

	@PostExchange(value = "/v1/embeddings")
	OpenAiClient.EmbeddingResponse embeddings(@RequestBody OpenAiClient.EmbeddingRequest embeddingRequest);
}
