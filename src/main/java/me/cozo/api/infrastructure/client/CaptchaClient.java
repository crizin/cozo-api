package me.cozo.api.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

@Slf4j
@Component
public class CaptchaClient {

	private final String secret;
	private final CaptchaClientExchanger captchaClientExchanger;

	public CaptchaClient(@Value("${cozo.turnstile.url}") String baseUrl, @Value("${cozo.turnstile.secret}") String secret) {
		this.secret = secret;
		this.captchaClientExchanger = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(WebClient.builder().baseUrl(baseUrl).build())).build()
			.createClient(CaptchaClientExchanger.class);
	}

	public boolean check(String token, String remoteIp) {
		if (StringUtils.isBlank(secret)) {
			log.debug("Skip turnstile captcha check [response={}]", token);
			return true;
		}

		var response = captchaClientExchanger.checkCaptcha(secret, token, remoteIp);

		log.info("Captcha check [response={}]", response);

		return response.success();
	}

	record Response(boolean success, @JsonProperty("error-codes") List<String> errorCodes) {
	}
}
