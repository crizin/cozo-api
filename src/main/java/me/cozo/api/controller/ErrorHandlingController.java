package me.cozo.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import me.cozo.api.domain.dto.ResponseDto;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorHandlingController implements ErrorController {

	@RequestMapping("/error")
	@SuppressWarnings("java:S3752")
	public ResponseEntity<ResponseDto<Void>> handle(HttpServletResponse response) {
		var status = HttpStatus.valueOf(response.getStatus());
		return new ResponseEntity<>(ResponseDto.error(status.getReasonPhrase()), status);
	}
}
