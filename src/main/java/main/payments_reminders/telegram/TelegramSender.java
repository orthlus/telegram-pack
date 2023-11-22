package main.payments_reminders.telegram;

import main.payments_reminders.entity.RemindToSend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static main.common.telegram.TelegramPropsProvider.getAdminId;

@Component
public class TelegramSender extends DefaultAbsSender {
	private final KeyboardsProvider keyboards;
	protected TelegramSender(
			@Value("${payments.telegram.bot.token}")
			String token,
			KeyboardsProvider keyboards) {
		super(new DefaultBotOptions(), token);
		this.keyboards = keyboards;
	}

	public void sendRemind(RemindToSend remind) {
		String msg = remind.getName();
		InlineKeyboardMarkup keyboard = keyboards.getRemindButtons(remind);
		send(msg, keyboard);
	}

	private void send(String text, ReplyKeyboard keyboard) {
		SendMessage message = SendMessage.builder()
				.chatId(getAdminId())
				.text(text)
				.replyMarkup(keyboard)
				.build();
		try {
			execute(message);
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}
}
