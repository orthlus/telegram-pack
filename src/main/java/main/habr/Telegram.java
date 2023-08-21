package main.habr;

import main.common.telegram.CustomSpringWebhookBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Telegram extends CustomSpringWebhookBot {
	private final HabrBotConfig config;

	public Telegram(HabrBotConfig botConfig) {
		super(botConfig);
		config = botConfig;
	}

	@Override
	public void onWebhookUpdate(Update update) {
		if (!isAdmin(update)) return;

		send("работает");
	}

	public void sendChannelMessage(String message) {
		send(msg(config.getChannelId(), message).disableWebPagePreview(true));
	}
}
