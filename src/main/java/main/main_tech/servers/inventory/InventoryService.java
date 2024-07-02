package main.main_tech.servers.inventory;

import lombok.RequiredArgsConstructor;
import main.main_tech.servers.data.InventoryServerWithDomains;
import main.main_tech.servers.ruvds.ServerMapper;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class InventoryService {
	private final Repo repo;
	private final ServerMapper mapper;

	public Set<InventoryServerWithDomains> getServers() {
		return mapper.mapAggServers(repo.getServersWithDomains());
	}
}
