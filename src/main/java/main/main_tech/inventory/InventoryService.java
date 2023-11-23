package main.main_tech.inventory;

import lombok.RequiredArgsConstructor;
import main.main_tech.ruvds.api.RuvdsServer;
import main.main_tech.ruvds.api.ServerMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static org.mapstruct.factory.Mappers.getMapper;

@Component
@RequiredArgsConstructor
public class InventoryService {
	private final Repo repo;
	private final ServerMapper mapper = getMapper(ServerMapper.class);

	public Set<ServerDomains> getServers() {
		return mapper.mapAggServers(repo.getServersWithDomains());
	}

	public void updateServersFromRuvds(Set<RuvdsServer> ruvdsServers) {
		repo.updateServers(ruvdsServers);
	}

	public String getStringListForMonitoring() {
		return repo.getServers().stream()
				.filter(Server::activeMonitoring)
				.map(server -> server.address() + ":" + server.sshPort())
				.collect(Collectors.joining("\n"))
				+ "\n";
	}
}
