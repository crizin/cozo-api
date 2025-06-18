package me.cozo.api.infrastructure.client;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.crizin.webs.Webs;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class DiscordClient {

	private final String url;

	public DiscordClient(@Value("${cozo.discord.url}") String url) {
		this.url = url;
	}

	public boolean sendMessage(String message) {
		if (StringUtils.isBlank(url)) {
			log.debug("Skip Discord message send [message={}]", message);
			return true;
		}

		try (Webs webs = Webs.createSimple()) {
			var response = webs.post(url).jsonPayload(Map.of("username", "cozo", "content", message)).fetch();
			return response.statusCode() == HttpStatus.SC_NO_CONTENT;
		}
	}
}
