package me.cozo.api.infrastructure.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cozo.openai")
public class OpenAiProperty {

	private String key;
	private String url;
}
