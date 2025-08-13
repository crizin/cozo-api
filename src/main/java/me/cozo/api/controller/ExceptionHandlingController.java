package me.cozo.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.domain.dto.ResponseDto;
import me.cozo.api.infrastructure.exception.ServiceException;
import org.apache.commons.lang3.Strings;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlingController {

	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ResponseDto<Void>> handle(ServiceException e) {
		log.trace(e.getMessage(), e);

		var responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
		Objects.requireNonNull(responseStatus);
		return new ResponseEntity<>(
			ResponseDto.error(Objects.toString(e.getMessage(), e.getClass().getSimpleName().replaceFirst("Exception$", ""))),
			responseStatus.code()
		);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ResponseDto<Void>> handle(MethodArgumentTypeMismatchException e) {
		return new ResponseEntity<>(
			ResponseDto.error(Objects.toString(e.getMessage(), e.getClass().getSimpleName().replaceFirst("Exception$", ""))),
			HttpStatus.BAD_REQUEST
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handle(HttpServletRequest request, Exception e) {
		var status = HttpStatus.INTERNAL_SERVER_ERROR;
		var message = "잠시 후 다시 시도해주세요";

		if (e instanceof ErrorResponse errorResponse) {
			var newStatus = HttpStatus.resolve(errorResponse.getStatusCode().value());
			if (newStatus != null) {
				status = newStatus;
				message = newStatus.getReasonPhrase();
			}
		}

		if (status.is5xxServerError()) {
			log.error(e.getMessage(), e);
		} else {
			log.debug(e.getMessage(), e);
		}

		if (Strings.CI.contains(request.getHeader("Accept"), "text/event-stream")) {
			return ResponseEntity.status(status).body(message);
		}

		return new ResponseEntity<>(ResponseDto.error(message), status);
	}
}
