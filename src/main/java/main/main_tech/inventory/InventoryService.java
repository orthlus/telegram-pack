package main.main_tech.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class InventoryService {
	private final Repo repo;

	public Set<Server> getServers() {
		return repo.getServers();
	}

}
