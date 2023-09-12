package main.main_tech.inventory;

import lombok.RequiredArgsConstructor;
import main.main_tech.ruvds.api.RuvdsServer;
import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Set;

import static main.Tables.TECH_INVENTORY_SERVERS;
import static org.jooq.Records.mapping;

@Component
@RequiredArgsConstructor
public class Repo {
	private final DSLContext db;

	public void updateServer(RuvdsServer ruvdsServer) {
		db.update(TECH_INVENTORY_SERVERS)
				.set(TECH_INVENTORY_SERVERS.CPU, ruvdsServer.cpu())
				.set(TECH_INVENTORY_SERVERS.RAM, ruvdsServer.ramGb())
				.set(TECH_INVENTORY_SERVERS.DRIVE, ruvdsServer.driveGb())
				.set(TECH_INVENTORY_SERVERS.ADD_DRIVE, ruvdsServer.additionalDriveGb())
				.set(TECH_INVENTORY_SERVERS.NAME, ruvdsServer.name())
				.where(TECH_INVENTORY_SERVERS.HOSTING_ID.eq(String.valueOf(ruvdsServer.id())))
				.execute();
	}

	@CacheEvict(value = "inventory-servers", allEntries = true)
	public void updateServers(Set<RuvdsServer> ruvdsServers) {
		for (RuvdsServer ruvdsServer : ruvdsServers) {
			updateServer(ruvdsServer);
		}
	}

	@Cacheable("inventory-servers")
	public Set<Server> getServers() {
		return db.select(
				TECH_INVENTORY_SERVERS.ID,
				TECH_INVENTORY_SERVERS.ADDRESS,
				TECH_INVENTORY_SERVERS.SSH_PORT,
				TECH_INVENTORY_SERVERS.NAME,
				TECH_INVENTORY_SERVERS.CPU,
				TECH_INVENTORY_SERVERS.RAM,
				TECH_INVENTORY_SERVERS.DRIVE,
				TECH_INVENTORY_SERVERS.ADD_DRIVE,
				TECH_INVENTORY_SERVERS.HOSTING_ID,
				TECH_INVENTORY_SERVERS.OS,
				TECH_INVENTORY_SERVERS.ACTIVE_MONITORING)
				.from(TECH_INVENTORY_SERVERS)
				.fetchSet(mapping(Server::new));
	}

	@CacheEvict(value = "inventory-servers", allEntries = true)
	public void setSshPortById(int serverId, int sshPort) {
		db.update(TECH_INVENTORY_SERVERS)
				.set(TECH_INVENTORY_SERVERS.SSH_PORT, sshPort)
				.where(TECH_INVENTORY_SERVERS.ID.eq(serverId))
				.execute();
	}

	@CacheEvict(value = "inventory-servers", allEntries = true)
	public void setIpAddressById(int serverId, String ipAddress) {
		db.update(TECH_INVENTORY_SERVERS)
				.set(TECH_INVENTORY_SERVERS.ADDRESS, ipAddress)
				.where(TECH_INVENTORY_SERVERS.ID.eq(serverId))
				.execute();
	}
}
