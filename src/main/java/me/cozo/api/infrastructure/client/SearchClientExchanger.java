package me.cozo.api.infrastructure.client;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import tools.jackson.databind.JsonNode;

interface SearchClientExchanger {

	@PostExchange(value = "/{index}/_analyze", contentType = MediaType.APPLICATION_JSON_VALUE)
	JsonNode analyze(@PathVariable String index, @RequestBody String body);
}
