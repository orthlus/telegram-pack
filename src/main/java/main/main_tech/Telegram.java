package main.main_tech;

import lombok.AllArgsConstructor;
import main.common.telegram.CustomSpringWebhookBot;
import main.common.telegram.Message;
import main.main_tech.inventory.InventoryService;
import main.main_tech.inventory.Server;
import main.main_tech.ruvds.api.RuvdsApi;
import main.main_tech.ruvds.api.RuvdsServer;
import main.main_tech.wg.WgService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Component
public class Telegram extends CustomSpringWebhookBot {
	@AllArgsConstructor
	private enum Commands {
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

	private final Map<String, Commands> commandsMap = new HashMap<>();
	{
		for (Commands command : Commands.values()) commandsMap.put(command.command, command);
	}

	public Telegram(Config botConfig,
					RuvdsApi ruvdsApi,
					WgService wg,
					RuvdsEmailClient ruvdsEmailClient,
					InventoryService inventoryService) {
		super(botConfig);
		this.ruvdsApi = ruvdsApi;
		this.wg = wg;
		this.ruvdsEmailClient = ruvdsEmailClient;
		this.inventoryService = inventoryService;
	}

	@Value("${main_tech.ruvds.api.domains}")
	private String ruvdsDomains;
	private final RuvdsApi ruvdsApi;
	private final WgService wg;
	private final RuvdsEmailClient ruvdsEmailClient;
	private final InventoryService inventoryService;
	private Message message1;
	private Message message2;

	public void sendAlarm1(String link) {
		message1 = sendWithOutPreview("Го!\n" + link);
	}

	public void deleteLastAlarmMessage1() {
		if (message1.notEmpty()) {
			deleteMessage(message1);
			message1 = Message.empty();
		}
	}

	public void sendAlarm2(String link) {
		message2 = sendWithOutPreview("Го!\n" + link);
	}

	public void deleteLastAlarmMessage2() {
		if (message2.notEmpty()) {
			deleteMessage(message2);
			message2 = Message.empty();
		}
	}

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
			case SERVERS -> {
				String text = inventoryService.getServers().stream()
						.map(Server::toString)
						.collect(joining("\n\n"));
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
			case UPDATE_SERVERS_FROM_RUVDS -> {
				inventoryService.updateServersFromRuvds(ruvdsApi.getServers());
				send("Ok");
			}
			case RUVDS_SERVERS -> {
				String[] domains = ruvdsDomains.split(",");
				List<RuvdsServer> sorted = new ArrayList<>(ruvdsApi.getServers());
				sorted.sort(Comparator.comparing(RuvdsServer::name));
				List<RuvdsServer> restServers = sorted.stream()
						.filter(s -> !contains(s.name(), domains))
						.toList();

				String result = stream(domains)
						.map(domain ->
								"<b>" + domain + ":</b>\n" + sorted.stream()
										.filter(server -> server.name().contains(domain))
										.map(server -> formatServer(domain, server))
										.collect(joining("\n"))
						)
						.collect(joining("\n\n"));

				if (!restServers.isEmpty()) {
					result += "\n\nunknown domains:\n";
					result += restServers.stream()
							.map(this::formatServer)
							.collect(joining("\n"));
				}

				send(msg(result).parseMode("html"));
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

	private String formatServer(String domain, RuvdsServer server) {
		String name = server.name()
				.replaceFirst(domain, "")
				.trim()
				.replaceAll(" +", " ");
		return "<b>%s</b>%n<code>  cpu: %d ram: %.1f Gb disk: %d Gb%n  %s</code>"
				.formatted(name, server.cpu(), server.ramGb(), server.driveGb(), server.id());
	}

	private String formatServer(RuvdsServer server) {
		return "<b>%s</b>%n<code>  cpu: %d ram: %.1f Gb disk: %d Gb  %s</code>"
				.formatted(server.name(), server.cpu(), server.ramGb(), server.driveGb(), server.id());
	}

	private boolean contains(String s, String[] toMatch) {
		for (String match : toMatch) if (s.contains(match)) return true;

		return false;
	}
}
