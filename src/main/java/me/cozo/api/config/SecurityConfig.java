package me.cozo.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final String serviceHost;

	public SecurityConfig(@Value("${cozo.service-host}") String serviceHost) {
		this.serviceHost = serviceHost;
	}

	@Bean
	@SuppressWarnings("java:S4502")
	public SecurityFilterChain filterChain(Environment environment, HttpSecurity httpSecurity) throws Exception {
		var isProduction = environment.acceptsProfiles(Profiles.of("production"));

		httpSecurity
			.httpBasic(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable);

		if (isProduction) {
			httpSecurity.headers(headers -> headers
				.httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable)
				.referrerPolicy(config -> config.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
				.contentSecurityPolicy(config -> config.policyDirectives(
					"default-src 'none'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'; script-src 'strict-dynamic' 'nonce-R4nd0m';"
				))
				.permissionsPolicyHeader(config -> config.policy("geolocation=(), microphone=(), camera=()"))
			);
		}

		return httpSecurity.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(serviceHost, "http://localhost:3000"));
		configuration.setAllowedMethods(List.of("GET", "POST"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
