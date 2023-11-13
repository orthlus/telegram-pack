package main.common.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static main.common.telegram.TelegramPropsProvider.getAdminId;
import static main.common.telegram.TelegramPropsProvider.getAppBaseUrl;

@Slf4j
public abstract class CustomSpringWebhookBot extends SpringWebhookBot {
	private final BotConfig botConfig;

	public CustomSpringWebhookBot(BotConfig botConfig) {
		super(new SetWebhook(getAppBaseUrl() + botConfig.getNickname()), botConfig.getToken());
		this.botConfig = botConfig;
	}

	public abstract void onWebhookUpdate(Update update);

	@Override
	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (!isAdmin(update)) return null;

		onWebhookUpdate(update);
		return null;
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

	public void send(String text) {
		send(getAdminId(), text);
	}

	public Message sendWithOutPreview(String text) {
		return send(msg(text).disableWebPagePreview(true));
	}

	public void sendByUpdate(String text, Update update) {
		send(update.getMessage().getChatId(), text);
	}

	public void send(long chatId, String text) {
		send(msg(chatId, text));
	}

	private Message send(SendMessage message) {
		try {
			var execute = execute(message);
			log.debug("{} - Sent message '{}'", this.getClass().getName(), message.getText());
			return new Message(execute.getChatId(), execute.getMessageId());
		} catch (TelegramApiException e) {
			log.error("{} - Error send message '{}'", this.getClass().getName(), message.getText(), e);
		}
		return Message.empty();
	}

	public Message send(String text, ReplyKeyboard keyboard) {
		return send(msg(text).replyMarkup(keyboard));
	}

	public Message send(SendMessage.SendMessageBuilder sendMessageBuilder) {
		return send(sendMessageBuilder.build());
	}

	public SendMessage.SendMessageBuilder msg(long chatId, String text) {
		return SendMessage.builder().chatId(chatId).text(text);
	}

	public SendMessage.SendMessageBuilder msg(String text) {
		return msg(getAdminId(), text);
	}

	public void deleteMessage(Update update) {
		deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
	}

	public void deleteMessage(CallbackQuery callbackQuery) {
		deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
	}

	public void deleteMessage(Message message) {
		deleteMessage(message.chatId(), message.messageId());
	}

	public void deleteMessage(long chatId, int messageId) {
		try {
			execute(new DeleteMessage(String.valueOf(chatId), messageId));
			log.debug("{} - Deleted message, chat {} messageId {}", this.getClass().getName(), chatId, messageId);
		} catch (TelegramApiException e) {
			log.error("{} - Error delete message, chat {} messageId {}", this.getClass().getName(), chatId, messageId, e);
		}
	}
}
