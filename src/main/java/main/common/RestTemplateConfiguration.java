package main.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfiguration {
	@Bean
	public RestTemplate wgRestTemplate(
			RestTemplateBuilder builder,
			@Value("${main_tech.api.url}") String url,
			@Value("${main_tech.api.user}") String user,
			@Value("${main_tech.api.secret}") String password) {
		return builder.basicAuthentication(user, password)
				.rootUri(url)
				.setConnectTimeout(Duration.ofMinutes(2))
				.build();
	}

	@Bean
	public RestTemplate chatWorkerRestTemplate(
			RestTemplateBuilder builder,
			@Value("${tasks.worker.url}") String url) {
		return builder
				.rootUri(url)
				.setConnectTimeout(Duration.ofMinutes(2))
				.build();
	}
}
