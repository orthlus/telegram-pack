package main.main_tech.inventory;

import main.main_tech.ruvds.api.RuvdsServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

@Component
public class NamingService {
	@Value("${main_tech.servers.domains}")
	private String serversDomains;
	private String[] domains;

	@PostConstruct
	private void init() {
		domains = serversDomains.split(",");
	}

	private boolean notContainsDomain(String s) {
		for (String match : domains) if (s.contains(match)) return false;

		return true;
	}

	private String dropDomains(String s) {
		for (String domain : domains) {
			s = s.replace(domain, "").trim();
		}
		return s;
	}

	public String formatDomainsRuvds(Set<RuvdsServer> servers) {
		List<RuvdsServer> list = sortRuvds(servers);
		List<RuvdsServer>[] lists = new List[domains.length + 1];
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < domains.length; i++) {
			lists[i] = new ArrayList<>(list.size());
			for (RuvdsServer server : list) {
				if (server.name().contains(domains[i])) {
					lists[i].add(server);
				}
			}
		}

		lists[lists.length - 1] = new ArrayList<>(list.size());
		for (RuvdsServer server : list) {
			if (notContainsDomain(server.name())) {
				lists[lists.length - 1].add(server);
			}
		}

		for (int i = 0; i < domains.length; i++) {
			sb.append("<b>")
					.append(domains[i])
					.append(":</b>\n");
			sb.append(formatAndJoinRuvds(lists[i]));
			sb.append("\n\n");
		}

		if (!lists[lists.length - 1].isEmpty()) {
			sb.append("<b>unknown domains:</b>\n");
			sb.append(formatAndJoinRuvds(lists[lists.length - 1]));
		} else {
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	public String formatDomains(Set<Server> servers) {
		List<Server> list = sort(servers);
		List<Server>[] lists = new List[domains.length + 1];
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < domains.length; i++) {
			lists[i] = new ArrayList<>(list.size());
			for (Server server : list) {
				if (server.name().contains(domains[i])) {
					lists[i].add(server);
				}
			}
		}

		lists[lists.length - 1] = new ArrayList<>(list.size());
		for (Server server : list) {
			if (notContainsDomain(server.name())) {
				lists[lists.length - 1].add(server);
			}
		}

		for (int i = 0; i < domains.length; i++) {
			sb.append("<b>")
					.append(domains[i])
					.append(":</b>\n");
			sb.append(formatAndJoin(lists[i]));
			sb.append("\n\n");
		}

		if (!lists[lists.length - 1].isEmpty()) {
			sb.append("<b>unknown domains:</b>\n");
			sb.append(formatAndJoin(lists[lists.length - 1]));
		} else {
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	private String formatAndJoinRuvds(List<RuvdsServer> servers) {
		return servers.stream()
				.map(this::format)
				.collect(joining("\n"));
	}

	private String formatAndJoin(List<Server> servers) {
		return servers.stream()
				.map(this::format)
				.collect(joining("\n"));
	}

	private List<RuvdsServer> sortRuvds(Set<RuvdsServer> set) {
		List<RuvdsServer> list = new ArrayList<>(set);
		list.sort(comparing(ruvdsServer -> dropDomains(ruvdsServer.name())));
		return list;
	}

	private List<Server> sort(Set<Server> set) {
		List<Server> list = new ArrayList<>(set);
		list.sort(comparing(server -> dropDomains(server.name())));
		return list;
	}

	private String format(Server s) {
		String name = dropDomains(s.name());
		if (s.addDrive() == null) {
			return """
				<b>%s</b>
				  <code>%s</code>:<code>%d</code>
				  <code>%d cpu %.1f Gb %d Gb</code>
				  <code>%s</code>, <u>%s</u>"""
					.formatted(
							name,
							s.address(), s.sshPort(),
							s.cpu(), s.ram(), s.drive(), s.os(), s.hostingName());
		} else {
			return """
				<b>%s</b>
				  <code>%s</code>:<code>%d</code>
				  <code>%d cpu %.1f Gb %d Gb (%d Gb)</code>
				  <code>%s</code>, <u>%s</u>"""
					.formatted(
							name,
							s.address(), s.sshPort(),
							s.cpu(), s.ram(), s.drive(), s.addDrive(), s.os(), s.hostingName());
		}
	}

	private String format(RuvdsServer s) {
		String name = dropDomains(s.name());
		if (s.additionalDriveGb() == null) {
			return """
					<b>%s</b>
					  <code>%d cpu %.1f Gb %d Gb</code>
					  <code>%s</code>"""
					.formatted(name, s.cpu(), s.ramGb(), s.driveGb(), s.id());
		} else {
			return """
					<b>%s</b>
					  <code>%d cpu %.1f Gb %d Gb (%d Gb)</code>
					  <code>%s</code>"""
					.formatted(name, s.cpu(), s.ramGb(), s.driveGb(), s.additionalDriveGb(), s.id());
		}
	}
}
