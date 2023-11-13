package main.main_tech;

import lombok.AllArgsConstructor;
import lombok.Getter;
import main.common.telegram.Command;
import main.common.telegram.CustomSpringWebhookBot;
import main.main_tech.inventory.InventoryService;
import main.main_tech.inventory.NamingService;
import main.main_tech.monitoring.MonitoringService;
import main.main_tech.ruvds.api.RuvdsApi;
import main.main_tech.wg.WgService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Component
public class Telegram extends CustomSpringWebhookBot {
	@AllArgsConstructor
	@Getter
	private enum Commands implements Command {
		UPDATE_MONITORING_FROM_DB("/update_monitoring_from_db"),
		SERVERS("/servers"),
		UPDATE_SERVERS_FROM_RUVDS("/update_servers_from_ruvds"),
		RUVDS_SERVERS("/ruvds_servers"),
		WG_STAT_DIFF("/wg_stat_diff"),
		WG_STAT_CURRENT("/wg_stat_current"),
		WG_UPDATE_USERS("/wg_update_users"),
		GET_CODE("/get_code"),
		GET_NEW_HOST("/get_new_host");
		final String command;
	}

	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);

	public Telegram(Config botConfig,
					RuvdsApi ruvdsApi,
					WgService wg,
					RuvdsEmailClient ruvdsEmailClient,
					InventoryService inventoryService,
					NamingService naming,
					MonitoringService monitoring) {
		super(botConfig);
		this.ruvdsApi = ruvdsApi;
		this.wg = wg;
		this.ruvdsEmailClient = ruvdsEmailClient;
		this.inventoryService = inventoryService;
		this.naming = naming;
		this.monitoring = monitoring;
	}

	private final RuvdsApi ruvdsApi;
	private final WgService wg;
	private final RuvdsEmailClient ruvdsEmailClient;
	private final InventoryService inventoryService;
	private final NamingService naming;
	private final MonitoringService monitoring;

	@Override
	public void onWebhookUpdate(Update update) {
		if (update.hasMessage()) {
			String messageText = update.getMessage().getText();

			if (commandsMap.containsKey(messageText)) {
				handleCommand(messageText);
			} else {
				handleText(messageText);
			}
		}
	}

	private void handleText(String messageText) {
		send("работает. команды:\n" + String.join("\n", commandsMap.keySet()));
	}

	private void handleCommand(String messageText) {
		switch (commandsMap.get(messageText)) {
			case UPDATE_MONITORING_FROM_DB -> {
				monitoring.updateMonitoringDataFromDb();
				send("Ok");
			}
			case SERVERS -> {
				String text = naming.formatDomains(inventoryService.getServers());
				send(msg(text).parseMode("html"));
			}
			case UPDATE_SERVERS_FROM_RUVDS -> {
				inventoryService.updateServersFromRuvds(ruvdsApi.getServers());
				send("Ok");
			}
			case RUVDS_SERVERS -> {
				String text = naming.formatDomainsRuvds(ruvdsApi.getServers());
				send(msg(text).parseMode("html"));
			}
			case WG_STAT_CURRENT -> {
				String text = wg.getPrettyCurrent();
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
			case WG_STAT_DIFF -> {
				String text = wg.getPrettyDiff();
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
				wg.saveCurrentItems();
			}
			case WG_UPDATE_USERS -> {
				wg.updateUsers();
				send("Ok");
			}
			case GET_CODE -> send(msg("<code>%s</code>".formatted(ruvdsEmailClient.getCode())).parseMode("html"));
			case GET_NEW_HOST -> send(msg("<code>%s</code>".formatted(ruvdsEmailClient.getNewHost())).parseMode("html"));
		}
	}
}
