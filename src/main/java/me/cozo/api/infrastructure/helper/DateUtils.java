package me.cozo.api.infrastructure.helper;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@UtilityClass
public class DateUtils {

	private enum Format {
		PATTERN_TIME1(
			"^(\\d{1,2}):(\\d{1,2}):(\\d{1,2})$",
			m -> LocalDateTime.now().withHour(i(m.group(1))).withMinute(i(m.group(2))).withSecond(i(m.group(1))).truncatedTo(ChronoUnit.SECONDS)
		),
		PATTERN_TIME2(
			"^(\\d{1,2}):(\\d{1,2})$",
			m -> LocalDateTime.now().withHour(i(m.group(1))).withMinute(i(m.group(2))).truncatedTo(ChronoUnit.MINUTES)
		),
		PATTERN_DATE1(
			"^(\\d{4})[./-](\\d{1,2})[./-](\\d{1,2})$",
			m -> LocalDate.of(i(m.group(1)), i(m.group(2)), i(m.group(3))).atStartOfDay()
		),
		PATTERN_DATE2(
			"^(\\d{1,2})[./-](\\d{1,2})[./-](\\d{1,2})$",
			m -> LocalDate.of(i("20" + m.group(1)), i(m.group(2)), i(m.group(3))).atStartOfDay()
		),
		PATTERN_DATE3(
			"^(\\d{1,2})[./-](\\d{1,2})$",
			m -> LocalDate.of(LocalDate.now().getYear(), i(m.group(1)), i(m.group(2))).atStartOfDay()
		),
		PATTERN_DATETIME1(
			"^(\\d{2})[./-](\\d{2})[/-](\\d{2}) (\\d{2}):(\\d{2})$",
			m -> LocalDateTime.of(i("20" + m.group(1)), i(m.group(2)), i(m.group(3)), i(m.group(4)), i(m.group(5)))
		),
		PATTERN_DATETIME2(
			"^(\\d{2})[./-](\\d{2})[./-](\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})$",
			m -> LocalDateTime.of(i("20" + m.group(1)), i(m.group(2)), i(m.group(3)), i(m.group(4)), i(m.group(5)), i(m.group(6)))
		),
		PATTERN_DATETIME3(
			"^(\\d{4})[./-](\\d{2})[./-](\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})$",
			m -> LocalDateTime.of(i(m.group(1)), i(m.group(2)), i(m.group(3)), i(m.group(4)), i(m.group(5)), i(m.group(6)))
		),
		PATTERN_DATETIME4(
			"^(\\d{4})년 (\\d{2})월 (\\d{2})일 (\\d{2})시 (\\d{2})분 (\\d{2})초$",
			m -> LocalDateTime.of(i(m.group(1)), i(m.group(2)), i(m.group(3)), i(m.group(4)), i(m.group(5)), i(m.group(6)))
		),
		PATTERN_DATETIME5(
			"^(\\d{2})[.-](\\d{2}) (\\d{2}):(\\d{2})$",
			m -> LocalDateTime.of(LocalDate.now().getYear(), i(m.group(1)), i(m.group(2)), i(m.group(3)), i(m.group(4)))
		),
		PATTERN_HUMAN_READABLE1(
			"^(\\d{1,2})분\\s*전$",
			m -> LocalDateTime.now().minusMinutes(i(m.group(1)))
		);

		private final Pattern pattern;
		private final Function<Matcher, LocalDateTime> mapper;

		private static int i(String string) {
			return TextUtils.number(string);
		}

		Format(String pattern, Function<Matcher, LocalDateTime> mapper) {
			this.pattern = Pattern.compile(pattern);
			this.mapper = mapper;
		}
	}

	public LocalDateTime parse(String string) {
		if (StringUtils.isBlank(string)) {
			return null;
		}

		return Arrays.stream(Format.values())
			.flatMap(format -> {
				Matcher matcher = format.pattern.matcher(string);
				return matcher.find() ? Stream.of(format.mapper.apply(matcher)) : Stream.empty();
			})
			.map(date -> date.isAfter(LocalDateTime.now()) ? date.minusDays(1) : date)
			.findFirst()
			.orElse(null);
	}

	public LocalDateTime getEndOfDay(LocalDate date) {
		return date.atTime(23, 59, 59, 999_999_999);
	}

	public Duration getElapsed(LocalDateTime time) {
		return Duration.between(time, LocalDateTime.now());
	}

	public String getHumanReadableTime(LocalDateTime time) {
		int hour = time.getHour();
		Duration elapsed = getElapsed(time);
		LocalDate today = LocalDate.now();

		if (elapsed.compareTo(Duration.of(1, ChronoUnit.MINUTES)) < 0) {
			return "방금";
		} else if (elapsed.compareTo(Duration.of(1, ChronoUnit.HOURS)) < 0) {
			return "%d분 전".formatted(Math.round(elapsed.get(ChronoUnit.SECONDS) / 60d));
		} else if (elapsed.compareTo(Duration.of(3, ChronoUnit.HOURS)) < 0) {
			return "%d시간 전".formatted(Math.round(elapsed.get(ChronoUnit.SECONDS) / 3600d));
		} else if (time.toLocalDate().isEqual(today)) {
			return getHumanReadableTimeHour(hour, "지난", "오늘");
		} else if (time.toLocalDate().isEqual(today.minusDays(1))) {
			return getHumanReadableTimeHour(hour, "어젯밤", "어제");
		} else if (time.getYear() == today.getYear()) {
			if (time.getMonth() == today.getMonth()) {
				return "%d일 %s".formatted(time.getDayOfMonth(), (hour < 12) ? "오전" : "오후");
			} else {
				return "%d월 %d일".formatted(time.getMonthValue(), time.getDayOfMonth());
			}
		} else if (elapsed.compareTo(Duration.ofDays(30)) < 0) {
			return "%d월 %d일".formatted(time.getMonthValue(), time.getDayOfMonth());
		} else if (time.getYear() == today.minusYears(1).getYear()) {
			return "작년 %d월".formatted(time.getMonthValue());
		} else {
			return "%d년".formatted(time.getYear());
		}
	}

	private String getHumanReadableTimeHour(int hour, String midnightPrefix, String dayPrefix) {
		if (hour == 0) {
			return "%s 자정".formatted(midnightPrefix);
		} else if (hour < 6) {
			return "%s 새벽 %d시".formatted(dayPrefix, hour);
		} else if (hour < 12) {
			return "%s 아침 %d시".formatted(dayPrefix, hour);
		} else if (hour == 12) {
			return "%s 오후 12시".formatted(dayPrefix);
		} else if (hour < 18) {
			return "%s 오후 %d시".formatted(dayPrefix, hour % 12);
		} else if (hour < 21) {
			return "%s 저녁 %d시".formatted(dayPrefix, hour % 12);
		} else {
			return "%s 밤 %d시".formatted(dayPrefix, hour % 12);
		}
	}
}
