package main.main_tech.inventory;

import lombok.RequiredArgsConstructor;
import main.main_tech.ruvds.api.ServerMapper;
import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static main.Tables.TECH_INVENTORY_SERVERS;
import static org.jooq.Records.mapping;
import static org.mapstruct.factory.Mappers.getMapper;

@Component
@RequiredArgsConstructor
public class Repo {
	private final DSLContext db;

	@CacheEvict(value = "inventory-servers", allEntries = true)
	public void saveServers(Set<Server> servers) {
		db.transaction(trx -> {
			trx.dsl().delete(TECH_INVENTORY_SERVERS).execute();

			trx.dsl().batchInsert(servers.stream()
							.map(server -> getMapper(ServerMapper.class).map(server))
							.collect(Collectors.toSet()))
					.execute();
		});
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
