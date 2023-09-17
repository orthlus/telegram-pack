package main.main_tech.inventory;

public record Server(
		int id,
		String address,
		Integer sshPort,
		String name,
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
