package main.main_tech.monitoring;

import lombok.RequiredArgsConstructor;
import main.main_tech.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonitoringService {
	@Value("${main_tech.monitoring.storage.bucket}")
	private String bucket;
	@Value("${main_tech.monitoring.storage.file}")
	private String file;
	private final MonitoringStorage monitoringStorage;
	private final InventoryService inventoryService;

	public void updateMonitoringDataFromDb() {
		monitoringStorage.uploadFileContent(bucket, file, inventoryService.getStringListForMonitoring());
	}
}
