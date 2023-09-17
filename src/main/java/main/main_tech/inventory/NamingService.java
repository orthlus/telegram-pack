package main.main_tech.inventory;

import main.main_tech.ruvds.api.RuvdsServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

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

	private String dropDomains(String s) {
		for (String domain : domains) {
			s = s.replace(domain, "").trim();
		}
		return s;
	}

	public String formatDomains(Set<Server> servers) {

	}

	public String format(Server s) {
		String name = dropDomains(s.name());
		return """
			<b>%s</b>
			<code>%s:%d
			%d cpu %.1f Gb %d Gb (%d Gb) %s</code>"""
				.formatted(
						name,
						s.address(), s.sshPort(),
						s.cpu(), s.ram(), s.drive(), s.addDrive(), s.os());
	}

	public String formatServer(String domain, RuvdsServer server) {
		String name = server.name()
				.replaceFirst(domain, "")
				.trim();
		return """
				<b>%s</b>
				  <code>cpu: %d ram: %.1f Gb disk: %d Gb
				  %s</code>"""
				.formatted(name, server.cpu(), server.ramGb(), server.driveGb(), server.id());
	}

	public String format(RuvdsServer server) {
		String name = dropDomains(server.name());
		return """
				<b>%s</b>
				  <code>cpu: %d ram: %.1f Gb disk: %d Gb  %s</code>"""
				.formatted(name, server.cpu(), server.ramGb(), server.driveGb(), server.id());
	}
}
