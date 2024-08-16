package main;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfiguration {
	@Bean
	public RestTemplate chatWorkerRestTemplate(
			RestTemplateBuilder builder,
			@Value("${tasks.worker.url}") String url) {
		return builder
				.rootUri(url)
				.setConnectTimeout(Duration.ofMinutes(2))
				.build();
	}

	@Bean
	public RestTemplate timewebRestTemplate(
			RestTemplateBuilder builder,
			@Value("${billing.timeweb.url}") String url,
			@Value("${billing.timeweb.token}") String token) {
		return builder
				.rootUri(url)
				.defaultHeader("authorization", "Bearer " + token.trim())
				.setConnectTimeout(Duration.ofMinutes(2))
				.build();
	}
}
