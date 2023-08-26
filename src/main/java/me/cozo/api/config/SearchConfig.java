package me.cozo.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "me.cozo.api.domain.repository.search")
public class SearchConfig {
}
