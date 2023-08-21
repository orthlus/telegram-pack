package main.main_tech;

import main.common.telegram.CustomSpringWebhookBot;
import main.common.telegram.Message;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class Telegram extends CustomSpringWebhookBot {
	private final Set<String> commands = new LinkedHashSet<>(List.of(
			"/wg_stat",
			"/wg_stat_diff",
			"/wg_stat_current",
			"/get_code",
			"/get_new_host"));
	public Telegram(Config botConfig, WgClient wgClient, RuvdsEmailClient ruvdsEmailClient) {
		super(botConfig);
		this.wgClient = wgClient;
		this.ruvdsEmailClient = ruvdsEmailClient;
	}

	private final WgClient wgClient;
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

	@Override
	public void onWebhookUpdate(Update update) {
		if (update.hasMessage()) {
			if (!isAdmin(update)) return;

			if (update.hasMessage()) {
				String messageText = update.getMessage().getText();

				if (commands.contains(messageText)) {
					handleCommand(messageText);
				} else {
					handleText(messageText);
				}
			}
		}
	}

	private void handleText(String messageText) {
		send("работает. команды:\n" + String.join("\n", commands));
	}

	private void handleCommand(String messageText) {
		switch (messageText) {
			case "/wg_stat_current", "/wg_stat" -> {
				String text = wgClient.getStat();
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
			case "/wg_stat_diff" -> {
				String text = wgClient.getRawStat();
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
			case "/get_code" -> send(msg("<code>%s</code>".formatted(ruvdsEmailClient.getCode())).parseMode("html"));
			case "/get_new_host" -> send(msg("<code>%s</code>".formatted(ruvdsEmailClient.getNewHost())).parseMode("html"));
		}
	}
}
