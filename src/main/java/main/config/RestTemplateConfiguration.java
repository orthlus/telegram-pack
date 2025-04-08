package main.config;

import art.aelaort.DefaultValues;
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
	public RestTemplate telegramListRestTemplate(RestTemplateBuilder builder) {
		DefaultValues defaultValues = new DefaultValues();
		return builder
				.rootUri(defaultValues.getUrl())
				.basicAuthentication(defaultValues.getUser(), defaultValues.getPassword())
				.build();
	}
}
