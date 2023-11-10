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
import static main.common.telegram.TelegramPropsProvider.getBotWebhookUrl;

@Slf4j
public abstract class CustomSpringWebhookBot extends SpringWebhookBot {
	private final BotConfig botConfig;

	public CustomSpringWebhookBot(BotConfig botConfig) {
		super(new SetWebhook(getBotWebhookUrl(botConfig)), botConfig.getToken());
		this.botConfig = botConfig;
	}

	public abstract void onWebhookUpdate(Update update);

	@Override
	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (!isBotPublic() && !isAdmin(update)) return null;

		onWebhookUpdate(update);
		return null;
	}

	private boolean isBotPublic() {
		return this instanceof PublicBot;
	}

	@Override
	public String getBotPath() {
		return getBotWebhookUrl(botConfig);
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

	public void updateMessageTextAndKeyboard(CallbackQuery callbackQuery, String text, InlineKeyboardMarkup keyboard) {
		updateMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), text, keyboard);
	}

	public void updateMessageKeyboard(CallbackQuery callbackQuery, InlineKeyboardMarkup keyboard) {
		updateMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), null, keyboard);
	}

	public void updateMessage(long chatId, int messageId, String text, InlineKeyboardMarkup keyboard) {
		try {
			EditMessageText msg = new EditMessageText();
			msg.setChatId(chatId);
			msg.setMessageId(messageId);
			if (keyboard != null)
				msg.setReplyMarkup(keyboard);
			if (text != null)
				msg.setText(text);
			execute(msg);
			log.debug("{} - Updated message, chat {} messageId {}", this.getClass().getName(), chatId, messageId);
		} catch (TelegramApiException e) {
			log.error("{} - Error update message, chat {} messageId {}", this.getClass().getName(), chatId, messageId, e);
		}
	}

	public void updateMessageText(CallbackQuery callbackQuery, String text) {
		updateMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), text, null);
	}

	@Deprecated
	public void updateMessageText(long chatId, int messageId, String text) {
		try {
			EditMessageText msg = new EditMessageText();
			msg.setChatId(chatId);
			msg.setMessageId(messageId);
			msg.setText(text);
			execute(msg);
			log.debug("{} - Updated message text, chat {} messageId {}", this.getClass().getName(), chatId, messageId);
		} catch (TelegramApiException e) {
			log.error("{} - Error update message text, chat {} messageId {}", this.getClass().getName(), chatId, messageId, e);
		}
	}

	public void sendVideo(long userId, String message, Path videoPath) {
		try {
			SendVideo video = SendVideo.builder()
					.chatId(userId)
					.caption(message)
					.video(new InputFile(videoPath.toFile()))
					.build();
			execute(video);
		} catch (TelegramApiException e) {
			log.error("Error send message", e);
		}
	}

	public void sendVideoByUpdate(Update update, String message, InputStream dataStream) {
		try {
			SendVideo video = SendVideo.builder()
					.chatId(update.getMessage().getChatId())
					.caption(message)
					.video(new InputFile(dataStream, UUID.randomUUID().toString()))
					.build();
			execute(video);
		} catch (TelegramApiException e) {
			log.error("Error send message", e);
		}
	}

	public void sendVideoByUpdate(Update update, String message, String videoUrl) {
		try {
			SendVideo video = SendVideo.builder()
					.chatId(update.getMessage().getChatId())
					.caption(message)
					.video(new InputFile(videoUrl))
					.build();
			execute(video);
		} catch (TelegramApiException e) {
			log.error("Error send message", e);
		}
	}

	public void sendPhotos(String userId, String message, List<String> photos) {
		try {
			List<InputMedia> medias = new LinkedList<>();
			for (String photoUrl : photos) {
				medias.add(new InputMediaPhoto(photoUrl));
			}
			medias.get(0).setCaption(message);
			SendMediaGroup sendMediaGroup = new SendMediaGroup(userId, medias);
			execute(sendMediaGroup);
		} catch (TelegramApiException e) {
			log.error("Error send message", e);
		}
	}

	public void sendPhoto(String userId, String message, String photoUrl) {
		try {
			SendPhoto sendPhoto = new SendPhoto(userId, new InputFile(photoUrl));
			sendPhoto.setCaption(message);
			execute(sendPhoto);
		} catch (TelegramApiException e) {
			log.error("Error send message", e);
		}
	}
}
