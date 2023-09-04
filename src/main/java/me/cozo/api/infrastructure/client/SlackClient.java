package me.cozo.api.infrastructure.client;

import io.micrometer.common.util.StringUtils;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.crizin.webs.Webs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlackClient {

	private final String url;

	public SlackClient(@Value("${cozo.slack.url}") String url) {
		this.url = url;
	}

	public boolean sendMessage(String message) {
		if (StringUtils.isBlank(url)) {
			log.debug("Skip Slack message send [message={}]", message);
			return true;
		}

		try (Webs webs = Webs.createSimple()) {
			var response = webs.post(url).jsonPayload(Map.of("text", message)).fetchAsString();
			return response.equals("ok");
		}
	}
}
