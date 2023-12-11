package main.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
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
	@Qualifier("regruWebClient")
	public WebClient regruWebClient(
			@Value("${regru.api.url}") String baseUrl
	) {
		return WebClient.builder()
				.baseUrl(baseUrl)
				.clientConnector(clientWithTimeout(2, MINUTES))
				.defaultHeaders(h -> h.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
				.exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> {
					ObjectMapper mapper = new ObjectMapper();
					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					MimeType mimeType = MimeTypeUtils.parseMimeType(MediaType.TEXT_PLAIN_VALUE);
					Jackson2JsonDecoder codec = new Jackson2JsonDecoder(mapper, mimeType);
					configurer.customCodecs().register(codec);
				}).build())
				.build();
	}

	@Bean
	@Qualifier("wgWebClient")
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
	@Qualifier("ruvdsWebClient")
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
