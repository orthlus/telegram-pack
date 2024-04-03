package main.main_tech.wg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WgClient {
//	@Qualifier("wgRestTemplate")
//	private final RestTemplate client;
	@Qualifier("wgRestClient")
	private final RestClient client2;

	String getRawStat() {
		return client2.get()
				.uri("/raw-stat")
				.retrieve()
				.body(String.class);
//		return client.getForEntity("/raw-stat", String.class).getBody();
	}

	String getUsers() {
		return client2.get()
				.uri("/users")
				.retrieve()
				.body(String.class);
//		return client
//				.getForEntity("/users", String.class)
//				.getBody();
	}
}
