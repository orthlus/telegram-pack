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
		execute(
				SendMessage.builder()
						.chatId(update.getMessage().getChatId())
						.text(String.join("\n", dataService.getTop5())),
				bashTelegramClient
		);
	}
}
