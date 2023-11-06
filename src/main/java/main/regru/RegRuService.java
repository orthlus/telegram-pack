package main.regru;

import lombok.RequiredArgsConstructor;
import main.regru.common.RR;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegRuService {
	private final RegRuClient regRuClient;
	private final Repo repo;

	public List<RR> getSavedSubdomainsList(String domainName) {
		return repo.getRecordsByDomain(domainName);
	}

	public List<RR> getSubdomainsList(String domainName) {
		List<RR> subdomainsList = regRuClient.getSubdomainsList(domainName);
		repo.saveDomains(subdomainsList, domainName);
		return subdomainsList;
	}

	public boolean addSubdomain(RR record, String domainName) {
		return regRuClient.addSubdomain(record, domainName);
	}

	public boolean deleteSubdomain(RR record, String domainName) {
		return regRuClient.deleteSubdomain(record, domainName);
	}
}
