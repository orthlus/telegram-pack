package main.main_tech.inventory;

public record ServerDomainsAgg(
		int id,
		String address,
		Integer sshPort,
		String name,
		String domainName,
		Integer cpu,
		Double ram,
		Integer drive,
		Integer addDrive,
		String hostingId,
		String os,
		boolean activeMonitoring,
		String hostingName
) {
}
