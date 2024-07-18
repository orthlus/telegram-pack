package main.tasks;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class KeyboardsProvider implements TgKeyboard {
	public InlineKeyboardMarkup finishButton(int messageId) {
		return inlineMarkup(row(btn("Завершено", String.valueOf(messageId))));
	}
}
