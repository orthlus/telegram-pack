package main.common.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static main.common.telegram.TelegramPropsProvider.getAdminId;

@Slf4j
@SuppressWarnings("deprecation")
public abstract class DefaultLongPollingBot extends TelegramLongPollingBot {
	@SuppressWarnings("UnusedReturnValue")
	public abstract BotApiMethod<?> onWebhookUpdateReceived(Update update);

	public abstract String getNickname();

	public abstract String getToken();

	@Override
	public void onUpdateReceived(Update update) {
		BotApiMethod<?> botApiMethod = onWebhookUpdateReceived(update);
		try {
			execute(botApiMethod);
		} catch (TelegramApiException e) {
			log.error("telegram execute error", e);
		}
	}

	@Override
	public String getBotToken() {
		return getToken();
	}

	@Override
	public String getBotUsername() {
		return getNickname();
	}

	protected SendMessage sendInCode(String text) {
		return send(msg("```sql\n%s\n```".formatted(text)).parseMode("markdown"));
	}

	protected SendMessage sendInMonospace(String text) {
		return send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
	}

	protected SendMessage send(String text) {
		return send(msg(text));
	}

	protected SendMessage send(SendMessage.SendMessageBuilder sendMessageBuilder) {
		return sendMessageBuilder.build();
	}

	protected SendMessage.SendMessageBuilder msg(String text) {
		return SendMessage.builder().chatId(getAdminId()).text(text);
	}
}
