package main.common.telegram;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static main.common.telegram.TelegramPropsProvider.getAdminId;

public interface DefaultWebhookBot {
	BotApiMethod<?> onWebhookUpdateReceived(Update update);

	String getNickname();

	default SendMessage sendInMonospace(String text) {
		return send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
	}

	default SendMessage send(String text) {
		return send(msg(text));
	}

	default SendMessage send(SendMessage.SendMessageBuilder sendMessageBuilder) {
		return sendMessageBuilder.build();
	}

	default SendMessage.SendMessageBuilder msg(String text) {
		return SendMessage.builder().chatId(getAdminId()).text(text);
	}
}
