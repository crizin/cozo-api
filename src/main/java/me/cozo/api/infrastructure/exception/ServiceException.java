package me.cozo.api.infrastructure.exception;

public abstract class ServiceException extends RuntimeException {

	protected ServiceException() {
		super();
	}

	protected ServiceException(Throwable cause) {
		super(cause);
	}

	protected ServiceException(String message) {
		super(message);
	}

	protected ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
