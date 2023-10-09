package me.cozo.api.infrastructure.client;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

interface CaptchaClientExchanger {

	@PostExchange(value = "/turnstile/v0/siteverify", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	CaptchaClient.Response checkCaptcha(@RequestParam String secret, @RequestParam String response, @RequestParam(name = "remoteip") String remoteIp);
}
