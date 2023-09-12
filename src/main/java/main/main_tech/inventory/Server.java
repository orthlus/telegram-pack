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
		boolean activeMonitoring
) {
	@Override
	public String toString() {
		return """
			%s
			%s:%d
			%d cpu %.1f Gb %d Gb (%d Gb) %s"""
				.formatted(
						name,
						address, sshPort,
						cpu, ram, drive, addDrive, os);
	}
}
