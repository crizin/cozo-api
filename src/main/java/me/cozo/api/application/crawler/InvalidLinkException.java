package me.cozo.api.application.crawler;

import me.cozo.api.infrastructure.exception.ServiceException;

public class InvalidLinkException extends ServiceException {

	public InvalidLinkException(String message) {
		super(message);
	}

	public InvalidLinkException(String message, Throwable cause) {
		super(message, cause);
	}
}
