package main.main_tech.ruvds.api;

import main.main_tech.inventory.Server;
import main.main_tech.inventory.ServerDomains;
import main.main_tech.inventory.ServerDomainsAgg;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;

@Mapper
public interface ServerMapper {
	ServerMapper Instance = Mappers.getMapper(ServerMapper.class);

	Set<ServerDomains> mapAggServers(Set<ServerDomainsAgg> s);

	@Mapping(target = "domains", expression = "java(Instance.parseDomains(s.domainName()))")
	ServerDomains map(ServerDomainsAgg s);

	default Set<String> parseDomains(String domainsStr) {
		Set<String> resultDomains = new HashSet<>();
		if (domainsStr != null && domainsStr.contains(",")) {
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
		}
		return resultDomains;
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
		return Instance.map(serversRaw.getServerRaws());
	}
}
