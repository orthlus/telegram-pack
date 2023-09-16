package main.main_tech.inventory;

import lombok.RequiredArgsConstructor;
import main.main_tech.ruvds.api.RuvdsServer;
import main.tables.TechInventoryServers;
import org.jooq.Condition;
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
	private final TechInventoryServers s = TECH_INVENTORY_SERVERS.as("s");

	public void updateServer(RuvdsServer o) {
		Condition condition = s.HOSTING_ID.eq(String.valueOf(o.id()))
				.and(s.HOSTING_NAME.eq("ruvds"));
		db.update(s)
				.set(s.CPU, o.cpu())
				.set(s.RAM, o.ramGb())
				.set(s.DRIVE, o.driveGb())
				.set(s.ADD_DRIVE, o.additionalDriveGb())
				.set(s.NAME, o.name())
				.where(condition)
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
				s.ID, s.ADDRESS, s.SSH_PORT, s.NAME,
				s.CPU, s.RAM, s.DRIVE, s.ADD_DRIVE,
				s.HOSTING_ID, s.OS, s.ACTIVE_MONITORING,
				s.HOSTING_NAME)
				.from(s)
				.fetchSet(mapping(Server::new));
	}

	@CacheEvict(value = "inventory-servers", allEntries = true)
	public void setSshPortById(int serverId, int sshPort) {
		db.update(s)
				.set(s.SSH_PORT, sshPort)
				.where(s.ID.eq(serverId))
				.execute();
	}

	@CacheEvict(value = "inventory-servers", allEntries = true)
	public void setIpAddressById(int serverId, String ipAddress) {
		db.update(s)
				.set(s.ADDRESS, ipAddress)
				.where(s.ID.eq(serverId))
				.execute();
	}
}
