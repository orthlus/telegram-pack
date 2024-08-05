package main.tasks;

import art.aelaort.TelegramKeyboard;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class KeyboardProvider implements TelegramKeyboard {
	public InlineKeyboardMarkup completeTaskButton(int messageId) {
		return inlineMarkup(row(btn("Завершить", String.valueOf(messageId))));
	}
}
