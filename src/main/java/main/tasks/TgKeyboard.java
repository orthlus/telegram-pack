package main.tasks;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Arrays;
import java.util.List;

import static org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton.builder;

public interface TgKeyboard {
	default InlineKeyboardMarkup inlineMarkup(InlineKeyboardRow... rows) {
		return new InlineKeyboardMarkup(Arrays.asList(rows));
	}

	default ButtonPair btn(String name, String query) {
		return new ButtonPair(name, query);
	}

	default InlineKeyboardRow row(ButtonPair... buttons) {
		List<InlineKeyboardButton> list = Arrays.stream(buttons)
				.map(buttonPair -> ((InlineKeyboardButton) builder()
						.text(buttonPair.name())
						.callbackData(buttonPair.query())
						.build()))
				.toList();
		return new InlineKeyboardRow(list);
	}

	record ButtonPair(String name, String query) {
	}
}
