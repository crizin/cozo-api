package me.cozo.api.config;

import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.function.BiConsumer;

@Configuration(proxyBeanMethods = false)
@EnableIntegration
public class IntegrationConfig implements BeanDefinitionRegistryPostProcessor {

	@Bean
	public IntegrationFlow integrationFlow() {
		return flow -> flow.<Object, Class<?>>route(
			Object::getClass,
			router -> scanServiceActivators(router::channelMapping)
		);
	}

	@Override
	public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
		scanServiceActivators((parameterType, channelName) ->
			registry.registerBeanDefinition(
				channelName,
				BeanDefinitionBuilder.genericBeanDefinition(ExecutorChannel.class)
					.addConstructorArgValue(channelTaskExecutor("Command-%s-".formatted(channelName)))
					.getBeanDefinition()
			)
		);
	}

	@Override
	public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// Do nothing
	}

	private void scanServiceActivators(BiConsumer<Class<?>, String> consumer) {
		Reflections reflections = new Reflections("me.cozo.api.application.handler");
		reflections.getTypesAnnotatedWith(Service.class)
			.forEach(type -> Arrays.stream(type.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(ServiceActivator.class))
				.forEach(method -> {
					var parameterType = method.getParameterTypes()[0];
					consumer.accept(parameterType, parameterType.getSimpleName());
				})
			);
	}

	private ThreadPoolTaskExecutor channelTaskExecutor(String prefix) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix(prefix);
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.initialize();
		return executor;
	}
}
