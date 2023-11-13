package main.common.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import static main.common.telegram.TelegramPropsProvider.getAdminId;
import static main.common.telegram.TelegramPropsProvider.getAppBaseUrl;

@Slf4j
public abstract class CustomSpringWebhookBot extends SpringWebhookBot {
	private final BotConfig botConfig;

	public CustomSpringWebhookBot(BotConfig botConfig) {
		super(new SetWebhook(getAppBaseUrl() + botConfig.getNickname()), botConfig.getToken());
		this.botConfig = botConfig;
	}

	public abstract BotApiMethod<?> onWebhookUpdate(Update update);

	@Override
	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (!isAdmin(update)) return null;

		return onWebhookUpdate(update);
	}

	@Override
	public String getBotPath() {
		return getAppBaseUrl() + botConfig.getNickname();
	}

	@Override
	public String getBotUsername() {
		return botConfig.getNickname();
	}

	public boolean isAdmin(Update update) {
		if (update.hasMessage())
			return getAdminId() == update.getMessage().getChat().getId();
		else if (update.hasCallbackQuery())
			return getAdminId() == update.getCallbackQuery().getMessage().getChat().getId();
		else
			return false;
	}

	public SendMessage send(String text) {
		return send(msg(text));
	}

	public SendMessage send(SendMessage.SendMessageBuilder sendMessageBuilder) {
		return sendMessageBuilder.build();
	}

	public SendMessage.SendMessageBuilder msg(String text) {
		return SendMessage.builder().chatId(getAdminId()).text(text);
	}
}
