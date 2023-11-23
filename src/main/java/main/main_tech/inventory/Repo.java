package main.main_tech.inventory;

import lombok.RequiredArgsConstructor;
import main.main_tech.ruvds.api.RuvdsServer;
import main.tables.TechInventoryDomainsRecords;
import main.tables.TechInventoryServers;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.Set;

import static main.Tables.TECH_INVENTORY_DOMAINS_RECORDS;
import static main.Tables.TECH_INVENTORY_SERVERS;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.groupConcat;

@Component
@RequiredArgsConstructor
public class Repo {
	private final DSLContext db;
	private final TechInventoryServers tis = TECH_INVENTORY_SERVERS;
	private final TechInventoryDomainsRecords tidr = TECH_INVENTORY_DOMAINS_RECORDS;

	public void updateServer(RuvdsServer o) {
		Condition condition = tis.HOSTING_ID.eq(String.valueOf(o.id()))
				.and(tis.HOSTING_NAME.eq("ruvds"));
		db.update(tis)
				.set(tis.CPU, o.cpu())
				.set(tis.RAM, o.ramGb())
				.set(tis.DRIVE, o.driveGb())
				.set(tis.ADD_DRIVE, o.additionalDriveGb())
				.set(tis.NAME, o.name())
				.where(condition)
				.execute();
	}

	public void updateServers(Set<RuvdsServer> ruvdsServers) {
		for (RuvdsServer ruvdsServer : ruvdsServers) {
			updateServer(ruvdsServer);
		}
	}

	public Set<ServerDomainsAgg> getServersWithDomains() {
		return db.select(
						tis.ID, tis.ADDRESS, tis.SSH_PORT, tis.NAME,
						groupConcat(concat(concat(tidr.SUB_DOMAIN, "."), tidr.CORE_DOMAIN)),
						tis.CPU, tis.RAM, tis.DRIVE, tis.ADD_DRIVE,
						tis.HOSTING_ID, tis.OS, tis.ACTIVE_MONITORING,
						tis.HOSTING_NAME)
				.from(tis)
				.leftJoin(tidr)
					.on(tis.ADDRESS.eq(tidr.ADDRESS))
				.groupBy(tis)
				.fetchSet(mapping(ServerDomainsAgg::new));
	}

	public Set<Server> getServers() {
		return db.select(
				tis.ID, tis.ADDRESS, tis.SSH_PORT, tis.NAME,
				tis.CPU, tis.RAM, tis.DRIVE, tis.ADD_DRIVE,
				tis.HOSTING_ID, tis.OS, tis.ACTIVE_MONITORING,
				tis.HOSTING_NAME)
				.from(tis)
				.fetchSet(mapping(Server::new));
	}

	public void setSshPortById(int serverId, int sshPort) {
		db.update(tis)
				.set(tis.SSH_PORT, sshPort)
				.where(tis.ID.eq(serverId))
				.execute();
	}

	public void setIpAddressById(int serverId, String ipAddress) {
		db.update(tis)
				.set(tis.ADDRESS, ipAddress)
				.where(tis.ID.eq(serverId))
				.execute();
	}
}
