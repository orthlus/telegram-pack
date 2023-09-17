package main.pg_chat;

import lombok.extern.slf4j.Slf4j;
import main.common.telegram.CustomSpringWebhookBot;
import main.common.telegram.PublicBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
//@Component
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
		if (update.hasMessage()) {
			if (update.getMessage().getChatId() == chatId) {
				if (update.getMessage().hasText()) {
					if (update.getMessage().getText().contains("/report")) {
						String msg = "@pgsql\nhttps://t.me/pgsql/" + update.getMessage().getMessageId();
						send(msg(channelId, msg).disableWebPagePreview(true));
					}
				}
			}
		}
	}
}
