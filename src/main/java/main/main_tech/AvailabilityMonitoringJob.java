package main.main_tech;

import lombok.RequiredArgsConstructor;
import main.main_tech.inventory.InventoryService;
import main.main_tech.inventory.Repo;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class AvailabilityMonitoringJob implements Job {
	private final InventoryService inventoryService;
	private final Repo repo;
	private final Telegram telegram;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		inventoryService.checkConnections(repo.getServers())
				.forEach((k, v) -> {
					if (!v) telegram.sendNotAvailableAlarm(k);
				});
	}
}
