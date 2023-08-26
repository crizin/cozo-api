package me.cozo.api.application.crawler;

import me.cozo.api.infrastructure.exception.ServiceException;
import org.jsoup.nodes.Element;

import java.util.Optional;

public class HtmlParsingException extends ServiceException {

	public HtmlParsingException(String message) {
		super(message);
	}

	public HtmlParsingException(Element element, String selector) {
		this("Not found element [selector=%s] in %s".formatted(
			selector, Optional.ofNullable(element).map(Element::html).map(HtmlParsingException::ellipsis).orElse(null)
		));
	}

	public HtmlParsingException(Element element, String selector, String attribute) {
		this("Not found element [selector=%s, attribute=%s] in %s".formatted(
			selector, attribute, Optional.ofNullable(element).map(Element::html).map(HtmlParsingException::ellipsis).orElse(null)
		));
	}

	private static String ellipsis(String string) {
		return string.substring(0, Math.min(string.length(), 1024));
	}
}
