package me.cozo.api.infrastructure.client;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Component
public class SlackClient {

	private final String token;
	private final String username;
	private final String channel;
	private final SlackClientExchanger slackClientExchanger;

	public SlackClient(
		@Value("${cozo.slack.url}") String baseUrl, @Value("${cozo.slack.token}") String token, @Value("${cozo.slack.username}") String username,
		@Value("${cozo.slack.channel}") String channel
	) {
		this.token = token;
		this.username = username;
		this.channel = channel;
		this.slackClientExchanger = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(WebClient.builder().baseUrl(baseUrl).build())).build().createClient(
			SlackClientExchanger.class);
	}

	public boolean sendMessage(String message) {
		if (StringUtils.isBlank(token)) {
			log.debug("Skip Slack message send [message={}]", message);
			return true;
		}

		return slackClientExchanger.postMessage(token, username, channel, message).ok();
	}

	record Response(boolean ok) {
	}
}
