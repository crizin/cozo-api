package me.cozo.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("Async-");
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.initialize();
		return executor;
	}

	@Bean
	public Executor indexExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("Async-Index-");
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.initialize();
		return executor;
	}

	@Bean
	public Executor linkExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("Async-Link-");
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.initialize();
		return executor;
	}
}
