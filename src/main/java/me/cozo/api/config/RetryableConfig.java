package me.cozo.api.config;

import me.cozo.api.application.crawler.ContentUnreachableFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;

import java.util.Optional;
import java.util.regex.Pattern;

@EnableRetry
@Configuration
public class RetryableConfig implements RetryListener {

	private static final Logger LOGGER = LoggerFactory.getLogger("retry");
	private static final Pattern SHORTEN_PACKAGE_PATTERN = Pattern.compile("(?<=[(. ])[a-z]+\\.|(public|void) ");

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		if (throwable instanceof ContentUnreachableFoundException) {
			return;
		}

		if (LOGGER.isErrorEnabled()) {
			LOGGER.error(
				"Retrying [count=%s, name=%s, exception=%s]".formatted(
					context.getRetryCount(),
					Optional.ofNullable(context.getAttribute(RetryContext.NAME))
						.map(String::valueOf)
						.map(SHORTEN_PACKAGE_PATTERN::matcher)
						.map(matcher -> matcher.replaceAll(StringUtils.EMPTY))
						.orElseGet(() -> (String) context.getAttribute(RetryContext.NAME)),
					throwable.getMessage()
				),
				throwable
			);
		}
	}
}
