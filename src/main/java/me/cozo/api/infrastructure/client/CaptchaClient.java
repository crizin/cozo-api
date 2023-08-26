package me.cozo.api.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Component
public class CaptchaClient {

	private final String secret;
	private final CaptchaClientExchanger captchaClientExchanger;

	public CaptchaClient(@Value("${cozo.recaptcha.url}") String baseUrl, @Value("${cozo.recaptcha.secret}") String secret) {
		this.secret = secret;
		this.captchaClientExchanger = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(WebClient.builder().baseUrl(baseUrl).build())).build()
			.createClient(CaptchaClientExchanger.class);
	}

	public boolean check(String token, String remoteIp) {
		if (StringUtils.isBlank(secret)) {
			log.debug("Skip Recaptcha check [response={}]", token);
			return true;
		}

		return captchaClientExchanger.checkCaptcha(secret, token, remoteIp).success();
	}

	record Response(boolean success) {
	}
}
