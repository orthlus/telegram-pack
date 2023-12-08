package main.main_tech.servers.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryServer {
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
}
