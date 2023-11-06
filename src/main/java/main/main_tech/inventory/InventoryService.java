package main.main_tech.inventory;

import lombok.RequiredArgsConstructor;
import main.main_tech.ruvds.api.RuvdsServer;
import main.main_tech.ruvds.api.ServerMapper;
import main.regru.PrivateBotConfig;
import main.regru.PublicBotConfig;
import main.regru.RegRuService;
import main.regru.common.RR;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mapstruct.factory.Mappers.getMapper;

@Component
@RequiredArgsConstructor
public class InventoryService {
	private final Repo repo;
	private final RegRuService regRuService;
	private final PrivateBotConfig privateBotConfig;
	private final PublicBotConfig publicBotConfig;

	public Set<Server> getServers() {
		Map<String, String> domainByIpMap = requestDomainsByIpMap(privateBotConfig.getDomainName());
		domainByIpMap.putAll(requestDomainsByIpMap(publicBotConfig.getDomainName()));

		ServerMapper mapper = getMapper(ServerMapper.class);

		return repo.getServers().stream()
				.map(s -> mapper.map(s, domainByIpMap.get(s.address())))
				.collect(Collectors.toSet());
	}

	private Map<String, String> requestDomainsByIpMap(String domain) {
		return groupDomainByIpAndConcatDomain(regRuService.getSavedSubdomainsList(domain), domain);
	}

	private Map<String, String> groupDomainByIpAndConcatDomain(List<RR> rrs, String domain) {
		Map<String, String> result = new HashMap<>();
		for (RR rr : rrs) {
			result.put(rr.ip(), "%s.%s".formatted(rr.domain(), domain));
		}
		return result;
	}

	public void updateServersFromRuvds(Set<RuvdsServer> ruvdsServers) {
		repo.updateServers(ruvdsServers);
	}

	public String getStringListForMonitoring() {
		return repo.getServers().stream()
				.filter(ServerDTO::activeMonitoring)
				.map(server -> server.address() + ":" + server.sshPort())
				.collect(Collectors.joining("\n"))
				+ "\n";
	}
}
