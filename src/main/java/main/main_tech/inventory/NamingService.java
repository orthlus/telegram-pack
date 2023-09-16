package main.main_tech.inventory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NamingService {
	@Value("${main_tech.servers.domains}")
	private String serversDomains;
	private String[] domains;

	@PostConstruct
	private void init() {
		domains = serversDomains.split(",");
	}

	public boolean containsDomain(String s) {
		for (String match : domains) if (s.contains(match)) return true;

		return false;
	}
}
