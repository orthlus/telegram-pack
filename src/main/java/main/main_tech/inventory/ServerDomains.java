package main.main_tech.inventory;

import main.main_tech.ServerWithName;

import java.util.Set;

public record ServerDomains(
		int id,
		String address,
		Integer sshPort,
		String name,
		Set<String> domains,
		Integer cpu,
		Double ram,
		Integer drive,
		Integer addDrive,
		String hostingId,
		String os,
		boolean activeMonitoring,
		String hostingName
) implements ServerWithName {
}
