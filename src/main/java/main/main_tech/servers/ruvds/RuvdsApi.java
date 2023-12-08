package main.main_tech.servers.ruvds;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.main_tech.servers.data.RuvdsServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuvdsApi {
	@Value("${main_tech.ruvds.api.url}")
	private String url;
	@Value("${main_tech.ruvds.api.token}")
	private String token;
	private RuvdsHttp client;
	private final ServerMapper mapper;

	@PostConstruct
	private void init() {
		client = Feign.builder()
				.decoder(new JacksonDecoder())
				.requestInterceptor(template -> template.header("Authorization", "Bearer " + token))
				.target(RuvdsHttp.class, url);
	}

	public Set<RuvdsServer> getServers() {
		return mapper.map(client.servers());
	}
}
