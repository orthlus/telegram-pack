package main.main_tech.servers.ruvds;

import lombok.RequiredArgsConstructor;
import main.main_tech.servers.data.RuvdsServer;
import main.main_tech.servers.ruvds.dto.ServersRaw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RuvdsApi {
	private WebClient client;
	private final ServerMapper mapper;

	@Autowired
	private void init(
			@Value("${main_tech.ruvds.api.url}") String url,
			@Value("${main_tech.ruvds.api.token}") String token
	) {
		client = WebClient.builder()
				.baseUrl(url)
				.defaultHeaders(h -> h.setBearerAuth(token))
				.build();
	}

	public Set<RuvdsServer> getServers() {
		return client.get()
				.uri("/servers")
				.retrieve()
				.bodyToMono(ServersRaw.class)
				.map(mapper::map)
				.block();
	}
}
