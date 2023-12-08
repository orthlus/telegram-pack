package main.main_tech.ruvds.api;

import main.main_tech.inventory.Server;
import main.main_tech.inventory.ServerDomains;
import main.main_tech.inventory.ServerDomainsAgg;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ServerMapper {
	Set<ServerDomains> mapAggServers(Set<ServerDomainsAgg> s);

	@Mapping(target = "domains", expression = "java(parseDomains(s.getDomainName()))")
	ServerDomains map(ServerDomainsAgg s);

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

	ServerDomainsAgg map(Server s, String domainName);

	@Mapping(target = "additionalDriveGb", source = "additionalDrive")
	@Mapping(target = "driveGb", source = "drive")
	@Mapping(target = "ramGb", source = "ram")
	@Mapping(target = "id", source = "virtualServerId")
	@Mapping(target = "name", source = "userComment")
	RuvdsServer map(ServerRaw s);

	Set<RuvdsServer> map(Set<ServerRaw> s);

	default Set<RuvdsServer> map(ServersRaw serversRaw) {
		return map(serversRaw.getServerRaws());
	}
}
