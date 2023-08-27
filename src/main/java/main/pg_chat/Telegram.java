package main.pg_chat;

import main.common.telegram.CustomSpringWebhookBot;
import main.common.telegram.PublicBot;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Telegram extends CustomSpringWebhookBot implements PublicBot {
	@Value("${pg_chat.telegram.channel.id}")
	private long channelId;
	@Value("${pg_chat.telegram.chat.id}")
	private long chatId;

	public Telegram(Pg botConfig) {
		super(botConfig);
	}

	@Override
	public void onWebhookUpdate(Update update) {

	}
}
