package main.main_tech.servers.inventory;

import main.main_tech.servers.data.InventoryServerWithDomains;
import main.main_tech.servers.data.ServerWithName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

@Component
public class NamingService {
	@Value("${main_tech.servers.domains}")
	private String[] domains;

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

	public String formatDomains(Set<InventoryServerWithDomains> servers) {
		List<InventoryServerWithDomains> list = sort(servers);
		List<InventoryServerWithDomains>[] lists = new List[domains.length + 1];
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < domains.length; i++) {
			lists[i] = new ArrayList<>(list.size());
			for (InventoryServerWithDomains server : list) {
				if (server.name().contains(domains[i])) {
					lists[i].add(server);
				}
			}
		}

		lists[lists.length - 1] = new ArrayList<>(list.size());
		for (InventoryServerWithDomains server : list) {
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

	private String formatAndJoin(List<InventoryServerWithDomains> servers) {
		return servers.stream()
				.map(this::format)
				.collect(joining("\n"));
	}

	private <T extends ServerWithName> List<T> sort(Set<T> set) {
		return set.stream()
				.sorted(comparing(server -> dropDomains(server.name())))
				.toList();
	}

	private String format(InventoryServerWithDomains s) {
		String name = dropDomains(s.name());
		String str = """
				<b>%s</b>
				  <code>%s</code>:<code>%d</code>$$$$
				  <code>%d cpu %.1f Gb %d Gb$$$</code>
				  <code>%s</code>, <u>%s</u>""";
		str = str.formatted(
							name,
							s.address(), s.sshPort(),
							s.cpu(), s.ram(), s.drive(),
							s.os(), s.hostingName());
		if (s.domains().isEmpty()) {
			str = str.replace("$$$$", "");
		} else {
			str = str.replace("$$$$", "\n  <b>%s</b>");
			str = str.formatted(String.join("\n  ", s.domains()));
		}

		if (s.addDrive() == null) {
			str = str.replace("$$$", "");
		} else {
			str = str.replace("$$$", " (%d Gb)");
			str = str.formatted(s.addDrive());
		}
		return str;
	}
}
