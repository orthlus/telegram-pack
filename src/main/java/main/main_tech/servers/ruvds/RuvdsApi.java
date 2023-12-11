package main.main_tech.servers.ruvds;

import lombok.RequiredArgsConstructor;
import main.main_tech.servers.data.RuvdsServer;
import main.main_tech.servers.ruvds.dto.ServersRaw;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RuvdsApi {
	@Qualifier("ruvdsWebClient")
	private final WebClient client;
	private final ServerMapper mapper;

	public Set<RuvdsServer> getServers() {
		return client.get()
				.uri("/servers")
				.retrieve()
				.bodyToMono(ServersRaw.class)
				.map(mapper::map)
				.block();
	}
}
