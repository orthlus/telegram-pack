package main.main_tech.servers.ruvds;

import main.main_tech.servers.data.InventoryServer;
import main.main_tech.servers.data.InventoryServerDomainsString;
import main.main_tech.servers.data.InventoryServerWithDomains;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ServerMapper {
	Set<InventoryServerWithDomains> mapAggServers(Set<InventoryServerDomainsString> s);

	@Mapping(target = "domains", expression = "java(parseDomains(s.getDomainName()))")
	InventoryServerWithDomains map(InventoryServerDomainsString s);

	default Set<String> parseDomains(String domainsStr) {
		if (domainsStr == null) return Set.of();

		if (domainsStr.contains(",")) {
			Set<String> resultDomains = new HashSet<>();
			String[] domains = domainsStr.split(",");
			for (String domain : domains) {
				if (domain.startsWith("@.")) {
					resultDomains.add(domain.replace("@.", ""));
				} else if (domain.startsWith("www.")) {
					resultDomains.add(domain.replace("www.", ""));
				} else {
					resultDomains.add(domain);
				}
			}
			return resultDomains;
		} else {
			return Set.of(domainsStr);
		}
	}

	InventoryServerDomainsString map(InventoryServer s, String domainName);
}
