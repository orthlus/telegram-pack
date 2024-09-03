package main;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import main.billing.BillingProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Setter
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfiguration {
	private final BillingProperties properties;

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
	public RestTemplate timewebRestTemplate(RestTemplateBuilder builder) {
		return builder
				.rootUri(properties.getTimewebUrl())
				.defaultHeader(AUTHORIZATION, "Bearer " + properties.getTimewebToken().trim())
				.setConnectTimeout(Duration.ofMinutes(2))
				.build();
	}

	@Bean
	public RestTemplate instagramRestTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.rootUri(properties.getInstagramUrl())
				.setConnectTimeout(Duration.ofMinutes(5))
				.setReadTimeout(Duration.ofMinutes(5))
				.defaultHeader("x-access-key", properties.getInstagramToken())
				.build();
	}

	@Bean
	public RestTemplate tiktokRestTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.rootUri(properties.getTiktokUrl())
				.setConnectTimeout(Duration.ofMinutes(5))
				.setReadTimeout(Duration.ofMinutes(5))
				.defaultHeader(AUTHORIZATION, "Bearer " + properties.getTiktokToken())
				.build();
	}

	@Bean
	public RestTemplate yandexRestTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.rootUri(properties.getYandexUrl())
				.setConnectTimeout(Duration.ofMinutes(5))
				.setReadTimeout(Duration.ofMinutes(5))
				.build();
	}
}
