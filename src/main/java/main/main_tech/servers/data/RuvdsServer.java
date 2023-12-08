package main.main_tech.servers.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class RuvdsServer implements ServerWithName {
	private String name;
	private long id;
	private int cpu;
	private double ramGb;
	private int driveGb;
	private int driveTariffId;
	private Integer additionalDriveGb;
	private Integer additionalDriveTariffId;
	private ServerStatus status;
	private int createProgress;
	private int tariffId;
	private int paymentPeriod;
	private Integer osId;
	private int ip;
}
