package main.main_tech.wg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WgClient {
	@Qualifier("wgRestTemplate")
	private final RestTemplate client;

	String getRawStat() {
		return client.getForObject("/raw-stat", String.class);
	}

	String getUsers() {
		return client.getForObject("/users", String.class);
	}
}
