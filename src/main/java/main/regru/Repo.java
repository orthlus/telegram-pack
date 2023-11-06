package main.regru;

import lombok.RequiredArgsConstructor;
import main.regru.common.RR;
import main.tables.TechInventoryDomainsRecords;
import main.tables.records.TechInventoryDomainsRecordsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static main.tables.TechInventoryDomainsRecords.TECH_INVENTORY_DOMAINS_RECORDS;
import static org.jooq.Records.mapping;

@Component
@RequiredArgsConstructor
public class Repo {
	private final DSLContext db;
	private final TechInventoryDomainsRecords tidr = TECH_INVENTORY_DOMAINS_RECORDS;

	public List<RR> getRecordsByDomain(String domain) {
		return db.select(tidr.ADDRESS, tidr.SUB_DOMAIN)
				.from(tidr)
				.where(tidr.CORE_DOMAIN.eq(domain))
				.fetch(mapping(RR::new));
	}

	public void saveDomains(List<RR> rrs, String domain) {
		db.transaction(trx -> {
			trx.dsl()
					.delete(tidr)
					.where(tidr.CORE_DOMAIN.eq(domain))
					.execute();

			trx.dsl().batchInsert(rrs.stream()
							.map(rr -> new TechInventoryDomainsRecordsRecord(rr.ip(), rr.domain(), domain))
							.toList())
					.execute();
		});
	}
}
