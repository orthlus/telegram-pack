package main.main_tech.servers.inventory;

import lombok.RequiredArgsConstructor;
import main.main_tech.servers.data.InventoryServerDomainsString;
import main.tables.TechInventoryDomainsRecords;
import main.tables.TechInventoryServers;
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

	public Set<InventoryServerDomainsString> getServersWithDomains() {
		return db.select(
						tis.ID, tis.ADDRESS, tis.SSH_PORT, tis.NAME,
						tis.CPU, tis.RAM, tis.DRIVE, tis.ADD_DRIVE,
						tis.HOSTING_ID, tis.OS, tis.ACTIVE_MONITORING,
						tis.HOSTING_NAME,
						groupConcat(concat(concat(tidr.SUB_DOMAIN, "."), tidr.CORE_DOMAIN))
				)
				.from(tis)
				.leftJoin(tidr)
					.on(tis.ADDRESS.eq(tidr.ADDRESS))
				.groupBy(tis)
				.fetchSet(mapping(InventoryServerDomainsString::new));
	}
}
