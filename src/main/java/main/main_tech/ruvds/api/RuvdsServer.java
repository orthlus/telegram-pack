package main.main_tech.ruvds.api;

import main.main_tech.ServerWithName;

public record RuvdsServer(
		String name,
		long id,
		int cpu,
		double ramGb,
		int driveGb,
		int driveTariffId,
		Integer additionalDriveGb,
		Integer additionalDriveTariffId,
		ServerStatus status,
		int createProgress,
		int tariffId,
		int paymentPeriod,
		Integer osId,
		int ip
) implements ServerWithName {
}
