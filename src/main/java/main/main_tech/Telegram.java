package main.main_tech;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.common.telegram.Command;
import main.common.telegram.DefaultLongPollingBot;
import main.main_tech.servers.inventory.InventoryService;
import main.main_tech.servers.inventory.NamingService;
import main.main_tech.servers.monitoring.MonitoringService;
import main.main_tech.servers.ruvds.RuvdsEmailClient;
import main.main_tech.wg.WgService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class Telegram extends DefaultLongPollingBot {
	@AllArgsConstructor
	@Getter
	private enum Commands implements Command {
		UPDATE_MONITORING_FROM_DB("/update_monitoring_from_db"),
		SERVERS("/servers"),
		WG_STAT_DIFF("/wg_stat_diff"),
		WG_STAT_CURRENT("/wg_stat_current"),
		WG_UPDATE_USERS("/wg_update_users"),
		GET_CODE("/get_code");
		final String command;
	}

	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);

	@Getter
	@Value("${main_tech.telegram.bot.nickname}")
	private String nickname;
	@Getter
	@Value("${main_tech.telegram.bot.token}")
	private String token;
	private final WgService wg;
	private final RuvdsEmailClient ruvdsEmailClient;
	private final InventoryService inventoryService;
	private final NamingService naming;
	private final MonitoringService monitoring;

	@Override
	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (update.hasMessage()) {
			String messageText = update.getMessage().getText();

			if (commandsMap.containsKey(messageText)) {
				return handleCommand(messageText);
			} else {
				return handleText();
			}
		}
		return null;
	}

	private BotApiMethod<?> handleText() {
		return send("работает. команды:\n" + String.join("\n", commandsMap.keySet()));
	}

	private BotApiMethod<?> handleCommand(String messageText) {
		return switch (commandsMap.get(messageText)) {
			case UPDATE_MONITORING_FROM_DB -> {
				monitoring.updateMonitoringDataFromDb();
				yield send("Ok");
			}
			case SERVERS -> {
				String text = naming.formatDomains(inventoryService.getServers());
				yield send(msg(text).parseMode("html"));
			}
			case WG_STAT_CURRENT -> {
				String text = wg.getPrettyCurrent();
				yield sendInMonospace(text);
			}
			case WG_STAT_DIFF -> {
				String text = wg.getPrettyDiff();
				wg.saveCurrentItems();
				yield sendInMonospace(text);
			}
			case WG_UPDATE_USERS -> {
				wg.updateUsers();
				yield send("Ok");
			}
			case GET_CODE -> sendInMonospace(ruvdsEmailClient.getCode());
		};
	}
}
