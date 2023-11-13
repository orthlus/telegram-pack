package main.main_tech;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

import static main.common.telegram.TelegramPropsProvider.getAdminId;

@Slf4j
@Component
public class TelegramSender extends DefaultAbsSender {
	private Optional<Message> message1;
	private Optional<Message> message2;

	protected TelegramSender(Config bot) {
		super(new DefaultBotOptions(), bot.getToken());
	}

	public void sendAlarm1(String link) {
		message1 = Optional.of(sendWithOutPreview("Го!\n" + link));
	}

	public void deleteLastAlarmMessage1() {
		if (message1.isPresent()) {
			deleteMessage(message1.get());
			message1 = Optional.empty();
		}
	}

	public void sendAlarm2(String link) {
		message2 = Optional.of(sendWithOutPreview("Го!\n" + link));
	}

	public void deleteLastAlarmMessage2() {
		if (message2.isPresent()) {
			deleteMessage(message2.get());
			message2 = Optional.empty();
		}
	}

	public void deleteMessage(Message message) {
		long chatId = message.getChatId();
		int messageId = message.getMessageId();
		try {
			execute(new DeleteMessage(String.valueOf(chatId), messageId));
		} catch (TelegramApiException e) {
			log.error("{} - Error delete message, chat {} messageId {}", this.getClass().getName(), chatId, messageId, e);
		}
	}

	public Message sendWithOutPreview(String text) {
		SendMessage message = SendMessage.builder()
				.chatId(getAdminId())
				.text(text)
				.disableWebPagePreview(true)
				.build();
		try {
			return execute(message);
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}
}
