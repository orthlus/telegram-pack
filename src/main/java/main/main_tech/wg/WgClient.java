package main.main_tech.wg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class WgClient {
	@Qualifier("wgRestClient")
	private final RestClient client2;

	String getRawStat() {
		return client2.get()
				.uri("/raw-stat")
				.retrieve()
				.body(String.class);
	}

	String getUsers() {
		return client2.get()
				.uri("/users")
				.retrieve()
				.body(String.class);
	}
}
