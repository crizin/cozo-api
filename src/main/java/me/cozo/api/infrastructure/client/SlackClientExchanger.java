package me.cozo.api.infrastructure.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

interface SlackClientExchanger {

	@PostExchange("/api/chat.postMessage")
	SlackClient.Response postMessage(@RequestParam String token, @RequestParam String username, @RequestParam String channel, @RequestParam String text);
}
