package main.main_tech.ruvds.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import main.main_tech.ServerWithName;

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
