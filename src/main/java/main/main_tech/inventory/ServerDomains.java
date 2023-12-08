package main.main_tech.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import main.main_tech.ServerWithName;

import java.util.Set;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public final class ServerDomains implements ServerWithName {
	private int id;
	private String address;
	private Integer sshPort;
	private String name;
	private Integer cpu;
	private Double ram;
	private Integer drive;
	private Integer addDrive;
	private String hostingId;
	private String os;
	private boolean activeMonitoring;
	private String hostingName;

	private Set<String> domains;
}
