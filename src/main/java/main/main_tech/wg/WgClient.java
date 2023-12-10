package main.main_tech.wg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Component
public class WgClient {
	private WebClient client;

	@Autowired
	private void init(
			@Value("${main_tech.api.url}") String url,
			@Value("${main_tech.api.user}") String user,
			@Value("${main_tech.api.secret}") String password
	) {
		HttpClient httpClient = HttpClient.create()
				.option(CONNECT_TIMEOUT_MILLIS, ((int) MINUTES.toMillis(2)));
		client = WebClient.builder()
				.baseUrl(url)
				.defaultHeaders(h -> h.setBasicAuth(user, password))
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}

	String getRawStat() {
		return client.get()
				.uri("/raw-stat")
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}

	String getUsers() {
		return client.get()
				.uri("/users")
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}
}
