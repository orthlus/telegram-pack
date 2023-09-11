package main.main_tech;

import main.common.telegram.CustomSpringWebhookBot;
import main.common.telegram.Message;
import main.main_tech.inventory.Server;
import main.main_tech.ruvds.api.RuvdsApi;
import main.main_tech.ruvds.api.RuvdsServer;
import main.main_tech.wg.WgService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Component
public class Telegram extends CustomSpringWebhookBot {
	private final Set<String> commands = new LinkedHashSet<>(List.of(
			"/ruvds_servers",
			"/wg_stat_diff",
			"/wg_stat_current",
			"/wg_update_users",
			"/get_code",
			"/get_new_host"));

	public Telegram(Config botConfig, RuvdsApi ruvdsApi, WgService wg, RuvdsEmailClient ruvdsEmailClient) {
		super(botConfig);
		this.ruvdsApi = ruvdsApi;
		this.wg = wg;
		this.ruvdsEmailClient = ruvdsEmailClient;
	}

	@Value("${main_tech.ruvds.api.domains}")
	private String ruvdsDomains;
	private final RuvdsApi ruvdsApi;
	private final WgService wg;
	private final RuvdsEmailClient ruvdsEmailClient;
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

	public void sendNotAvailableAlarm(Server server) {
		send("Сервер '%s' недоступен по порту %d".formatted(server.name(), server.sshPort()));
	}

	@Override
	public void onWebhookUpdate(Update update) {
		if (update.hasMessage()) {
			String messageText = update.getMessage().getText();

			if (commands.contains(messageText)) {
				handleCommand(messageText);
			} else {
				handleText(messageText);
			}
		}
	}

	private void handleText(String messageText) {
		send("работает. команды:\n" + String.join("\n", commands));
	}

	private void handleCommand(String messageText) {
		switch (messageText) {
			case "/ruvds_servers" -> {
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
			case "/wg_stat_current", "/wg_stat" -> {
				String text = wg.getPrettyCurrent();
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
			case "/wg_stat_diff" -> {
				String text = wg.getPrettyDiff();
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
				wg.saveCurrentItems();
			}
			case "/wg_update_users" -> {
				wg.updateUsers();
				send("Ok");
			}
			case "/get_code" -> send(msg("<code>%s</code>".formatted(ruvdsEmailClient.getCode())).parseMode("html"));
			case "/get_new_host" -> send(msg("<code>%s</code>".formatted(ruvdsEmailClient.getNewHost())).parseMode("html"));
		}
	}

	private String formatServer(String domain, RuvdsServer server) {
		String name = server.name()
				.replaceFirst(domain, "")
				.trim()
				.replaceAll(" +", " ");
		return format("<b>%s</b>%n<code>  cpu: %d ram: %.1f Gb disk: %d Gb</code>", name, server.cpu(), server.ramGb(), server.driveGb());
	}

	private String formatServer(RuvdsServer server) {
		return format("<b>%s</b>%n<code>  cpu: %d ram: %.1f Gb disk: %d Gb</code>",
				server.name(), server.cpu(), server.ramGb(), server.driveGb());
	}

	private boolean contains(String s, String[] toMatch) {
		for (String match : toMatch) if (s.contains(match)) return true;

		return false;
	}
}
