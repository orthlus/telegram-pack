package main.main_tech.wg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class WgClient {
	@Qualifier("wgWebClient")
	private final WebClient client;

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
