package me.cozo.api.infrastructure.helper;

import com.google.common.util.concurrent.RateLimiter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@UtilityClass
@SuppressWarnings("UnstableApiUsage")
public class RateLimiterHelper {

	private static final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

	public static void acquire(String host) {
		var slept = rateLimiters.computeIfAbsent(host, key -> RateLimiter.create(1)).acquire();
		if (slept > 0) {
			log.trace("RateLimiter slept [host={}, seconds={}]", host, slept);
		}
	}
}
