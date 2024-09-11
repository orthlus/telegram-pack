package main.bash;

import art.aelaort.SpringLongPollingBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static art.aelaort.TelegramClientHelpers.execute;

@Component
@RequiredArgsConstructor
public class BashBot implements SpringLongPollingBot {
	private final DataService dataService;
	private final TelegramClient bashTelegramClient;
	@Getter
	@Value("${bash.telegram.bot.token}")
	private String botToken;

	@Override
	public void consume(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			if (messageText.startsWith("/")) {
				send(update, "дай число");
			} else {
				try {
					int rank = Integer.parseInt(messageText);
					sendByRank(update, rank);
				} catch (NumberFormatException ignored) {
					send(update, "нет, дай число");
				}
			}
		}
	}

	private void sendByRank(Update update, int rank) {
		String text;
		try {
			text = dataService.getByRank(rank);
		} catch (Exception e) {
			text = "not found";
		}
		send(update, text);
	}

	private void send(Update update, String text) {
		execute(
				SendMessage.builder()
						.chatId(update.getMessage().getChatId())
						.text(text),
				bashTelegramClient
		);
	}
}
