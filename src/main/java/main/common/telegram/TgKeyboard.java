package main.common.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton.builder;

@SuppressWarnings("unchecked")
public interface TgKeyboard {

	default List<List<InlineKeyboardButton>> emptyKeyboard(int size) {
		return new ArrayList<>(size);
	}

	default InlineKeyboardMarkup inlineMarkup(List<List<InlineKeyboardButton>> keyboard) {
		return new InlineKeyboardMarkup(keyboard);
	}

	default InlineKeyboardMarkup inlineMarkup(List<InlineKeyboardButton>... rows) {
		return new InlineKeyboardMarkup(inlineTable(rows));
	}

	default List<List<InlineKeyboardButton>> inlineTable(List<InlineKeyboardButton>... rows) {
		return Arrays.asList(rows);
	}

	default ButtonPair btn() {
		return null;
	}

	default ButtonPair btn(String name, String query) {
		return new ButtonPair(name, query);
	}

	default List<InlineKeyboardButton> row(ButtonPair... buttons) {
		List<InlineKeyboardButton> result = new LinkedList<>();
		for (ButtonPair buttonPair : buttons) {
			InlineKeyboardButton button = buttonPair == null ?
					builder().text("_").callbackData("_").build() :
					builder().text(buttonPair.name()).callbackData(buttonPair.query()).build();
			result.add(button);
		}
		return result;
	}

	record ButtonPair(String name, String query) {
	}
}
