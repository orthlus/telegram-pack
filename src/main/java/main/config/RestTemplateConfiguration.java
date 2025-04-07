package main.config;

import art.aelaort.TelegramListProperties;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Setter
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfiguration {
	@Bean
	public RestTemplate telegramListRestTemplate(RestTemplateBuilder builder, TelegramListProperties properties) {
		return builder
				.rootUri(properties.getUrl())
				.basicAuthentication(properties.getUser(), properties.getPassword())
				.build();
	}
}
