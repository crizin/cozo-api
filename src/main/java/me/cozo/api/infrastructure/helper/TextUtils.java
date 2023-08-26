package me.cozo.api.infrastructure.helper;

import lombok.experimental.UtilityClass;
import me.cozo.api.application.crawler.InvalidLinkException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.regex.Pattern;

@UtilityClass
public class TextUtils {

	private final Pattern PATTERN_URL = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
	private final Pattern PATTERN_NUMBER = Pattern.compile(".*?(-?\\d+).*?");
	private final Pattern PATTERN_SHORT_NUMBER = Pattern.compile("^(\\d+(?:\\.\\d+)?)([kmg만])$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private final Pattern PATTERN_WHITESPACE = Pattern.compile("(\\s|\\u00A0|&nbsp;?)++", Pattern.CASE_INSENSITIVE);

	public String getFragment(String string, String pattern) {
		return getFragmentOptional(string, pattern).orElseThrow(
			() -> new IllegalArgumentException("Not found pattern [pattern=%s, string=%s]".formatted(pattern, string))
		);
	}

	public Optional<String> getFragmentOptional(String string, String pattern) {
		var p = Pattern.compile(pattern);
		var m = p.matcher(string);
		if (!m.find()) {
			return Optional.empty();
		}
		return Optional.of(m.group(1));
	}

	public int number(String string) {
		return number(string, 0);
	}

	public int number(String string, int defaultValue) {
		if (StringUtils.isBlank(string)) {
			return defaultValue;
		}

		var matcher = PATTERN_SHORT_NUMBER.matcher(string);

		if (matcher.find()) {
			var value = switch (matcher.group(2)) {
				case "k" -> new BigDecimal(matcher.group(1)).multiply(BigDecimal.valueOf(1_000));
				case "m" -> new BigDecimal(matcher.group(1)).multiply(BigDecimal.valueOf(1_000_000));
				case "g" -> new BigDecimal(matcher.group(1)).multiply(BigDecimal.valueOf(1_000_000_000));
				case "만" -> new BigDecimal(matcher.group(1)).multiply(BigDecimal.valueOf(10_000));
				default -> null;
			};

			if (value != null) {
				return value.intValue();
			}
		}

		matcher = PATTERN_NUMBER.matcher(string);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : defaultValue;
	}

	public String compactWhitespace(String string) {
		return PATTERN_WHITESPACE.matcher(string).replaceAll(" ").trim();
	}

	public String removeUrl(String string) {
		return PATTERN_URL.matcher(string).replaceAll(" ");
	}

	public String extractHost(String url) {
		try {
			var host = new URI(url).getHost();
			if (host == null) {
				throw new InvalidLinkException("Host must be not null [url=%s]".formatted(url));
			}
			return StringUtils.removeStart(host, "www.");
		} catch (URISyntaxException e) {
			throw new InvalidLinkException("Host must be not null [url=%s]".formatted(url), e);
		}
	}
}
