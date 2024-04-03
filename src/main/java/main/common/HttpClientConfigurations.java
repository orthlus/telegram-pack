package main.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class HttpClientConfigurations {
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
	public RestClient wgRestClient(
			@Value("${main_tech.api.url}") String url,
			@Value("${main_tech.api.user}") String user,
			@Value("${main_tech.api.secret}") String password
	) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMinutes(2));
		requestFactory.setReadTimeout(Duration.ofMinutes(2));

		return RestClient.builder()
				.baseUrl(url)
				.requestInterceptor(new BasicAuthenticationInterceptor(user, password))
				.requestFactory(new SimpleClientHttpRequestFactory())
				.build();
	}
}
