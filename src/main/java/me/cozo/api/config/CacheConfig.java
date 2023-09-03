package me.cozo.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
public class CacheConfig {

	@Bean
	@Primary
	public CacheManager oneMinuteCacheManager(RedisConnectionFactory connectionFactory) {
		return getCacheManager(connectionFactory, Duration.ofMinutes(1));
	}

	@Bean
	public CacheManager oneDayCacheManager(RedisConnectionFactory connectionFactory) {
		return getCacheManager(connectionFactory, Duration.ofDays(1));
	}

	private CacheManager getCacheManager(RedisConnectionFactory connectionFactory, Duration duration) {
		var redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(duration)
			.serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));

		return RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
			.cacheDefaults(redisCacheConfiguration).build();
	}
}
