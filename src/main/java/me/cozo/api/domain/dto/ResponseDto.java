package me.cozo.api.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;

@Getter
public class ResponseDto<T> {

	private final boolean error;

	@JsonInclude(Include.NON_NULL)
	private final String errorMessage;

	@JsonInclude(Include.NON_NULL)
	private final T result;

	public static <T> ResponseDto<T> success() {
		return new ResponseDto<>(false, null, null);
	}

	public static <T> ResponseDto<T> success(T result) {
		return new ResponseDto<>(false, null, result);
	}

	public static <T> ResponseDto<T> error(String message) {
		return new ResponseDto<>(true, message, null);
	}

	private ResponseDto(boolean error, String errorMessage, T result) {
		this.error = error;
		this.errorMessage = errorMessage;
		this.result = result;
	}
}
