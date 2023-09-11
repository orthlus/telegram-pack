package main.main_tech;

import lombok.RequiredArgsConstructor;
import main.main_tech.inventory.InventoryService;
import main.main_tech.inventory.Repo;
import main.main_tech.inventory.Server;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class AvailabilityMonitoringJob implements Job {
	private final InventoryService inventoryService;
	private final Repo repo;
	private final Telegram telegram;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Set<Server> servers = repo.getServers()
				.stream()
				.filter(Server::activeMonitoring)
				.collect(Collectors.toSet());

		inventoryService.checkConnections(servers)
				.forEach((k, v) -> {
					if (!v) telegram.sendNotAvailableAlarm(k);
				});
	}
}
