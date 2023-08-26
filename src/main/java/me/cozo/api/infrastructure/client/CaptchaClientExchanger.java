package me.cozo.api.infrastructure.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

interface CaptchaClientExchanger {

	@PostExchange("/recaptcha/api/siteverify")
	CaptchaClient.Response checkCaptcha(@RequestParam String secret, @RequestParam String response, @RequestParam(name = "remoteip") String remoteIp);
}
