package main.config;

import art.aelaort.TelegramListProperties;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Setter
@Configuration
@RequiredArgsConstructor
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
	public RestTemplate billingServiceRestTemplate(
			RestTemplateBuilder builder,
			@Value("${billing.service.url}") String url) {
		return builder
				.rootUri(url)
				.setConnectTimeout(Duration.ofMinutes(2))
				.build();
	}

	@Bean
	public RestTemplate bashServiceRestTemplate(
			RestTemplateBuilder builder,
			@Value("${bash.service.url}") String url) {
		return builder
				.rootUri(url)
				.setConnectTimeout(Duration.ofMinutes(2))
				.build();
	}

	@Bean
	public RestTemplate telegramListRestTemplate(RestTemplateBuilder builder, TelegramListProperties properties) {
		return builder
				.rootUri(properties.getUrl())
				.basicAuthentication(properties.getUser(), properties.getPassword())
				.build();
	}
}
