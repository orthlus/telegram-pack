package main.main_tech.inventory;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.Set;

import static main.Tables.TECH_INVENTORY_SERVERS;
import static org.jooq.Records.mapping;

@Component
@RequiredArgsConstructor
public class Repo {
	private final DSLContext db;

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
				TECH_INVENTORY_SERVERS.OS)
				.from(TECH_INVENTORY_SERVERS)
				.fetchSet(mapping(Server::new));
	}

	public void setSshPortById(int serverId, int sshPort) {
		db.update(TECH_INVENTORY_SERVERS)
				.set(TECH_INVENTORY_SERVERS.SSH_PORT, sshPort)
				.where(TECH_INVENTORY_SERVERS.ID.eq(serverId))
				.execute();
	}

	public void setIpAddressById(int serverId, String ipAddress) {
		db.update(TECH_INVENTORY_SERVERS)
				.set(TECH_INVENTORY_SERVERS.ADDRESS, ipAddress)
				.where(TECH_INVENTORY_SERVERS.ID.eq(serverId))
				.execute();
	}
}
