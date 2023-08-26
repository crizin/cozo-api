package me.cozo.api.application.crawler;

import me.cozo.api.infrastructure.exception.ServiceException;

public class ContentNotFoundException extends ServiceException {

	public ContentNotFoundException(String message) {
		super(message);
	}
}
