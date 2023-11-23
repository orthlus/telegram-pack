package main.main_tech.inventory;

import lombok.RequiredArgsConstructor;
import main.main_tech.ruvds.api.RuvdsServer;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventoryService {
	private final Repo repo;

	public Set<Server> getServers() {
		return repo.getServersWithDomains();
	}

	public void updateServersFromRuvds(Set<RuvdsServer> ruvdsServers) {
		repo.updateServers(ruvdsServers);
	}

	public String getStringListForMonitoring() {
		return repo.getServers().stream()
				.filter(ServerDTO::activeMonitoring)
				.map(server -> server.address() + ":" + server.sshPort())
				.collect(Collectors.joining("\n"))
				+ "\n";
	}
}
