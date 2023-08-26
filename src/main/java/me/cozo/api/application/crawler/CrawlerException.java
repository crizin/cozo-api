package me.cozo.api.application.crawler;

import me.cozo.api.infrastructure.exception.ServiceException;

public class CrawlerException extends ServiceException {

	public CrawlerException(Throwable cause) {
		super(cause);
	}
}
