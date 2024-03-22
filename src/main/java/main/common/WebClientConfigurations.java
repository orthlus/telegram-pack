package main.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
public class WebClientConfigurations {
	private ReactorClientHttpConnector clientWithTimeout(int n, TimeUnit timeUnit) {
		HttpClient httpClient = HttpClient.create().option(CONNECT_TIMEOUT_MILLIS, ((int) timeUnit.toMillis(n)));
		return new ReactorClientHttpConnector(httpClient);
	}

	@Bean
	public WebClient wgWebClient(
			@Value("${main_tech.api.url}") String url,
			@Value("${main_tech.api.user}") String user,
			@Value("${main_tech.api.secret}") String password
	) {
		return WebClient.builder()
				.baseUrl(url)
				.defaultHeaders(h -> h.setBasicAuth(user, password))
				.clientConnector(clientWithTimeout(2, MINUTES))
				.build();
	}

	@Bean
	public WebClient ruvdsWebClient(
			@Value("${main_tech.ruvds.api.url}") String url,
			@Value("${main_tech.ruvds.api.token}") String token
	) {
		return WebClient.builder()
				.baseUrl(url)
				.defaultHeaders(h -> h.setBearerAuth(token))
				.build();
	}
}
