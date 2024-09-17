package main.bash;

import art.aelaort.SpringLongPollingBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static art.aelaort.TelegramClientHelpers.execute;

@Component
@RequiredArgsConstructor
public class BashBot implements SpringLongPollingBot {
	private final TelegramClient bashTelegramClient;
	private final DataService dataService;
	@Getter
	@Value("${bash.telegram.bot.token}")
	private String botToken;

	@Override
	public void consume(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			handleText(update);
		} else if (update.hasInlineQuery()) {
			handleInlineQuery(update);
		}
	}

	private void handleInlineQuery(Update update) {
		InlineQuery inlineQuery = update.getInlineQuery();
		String query = inlineQuery.getQuery();
		try {
			int rank = Integer.parseInt(query);
//			String text = getByRank(rank);
			/*bashTelegramClient.execute(AnswerInlineQuery.builder()
					.result(InlineQueryResult)
					.build());*/
		} catch (NumberFormatException ignored) {
		}
	}

	private void handleText(Update update) {
		String messageText = update.getMessage().getText();
		if (messageText.startsWith("/")) {
			if (messageText.equals("/random")) {
				send(update, getRandom());
			} else {
				send(update, "дай число");
			}
		} else {
			try {
				int rank = Integer.parseInt(messageText);
				String text = getByRank(rank);
				send(update, text);
			} catch (NumberFormatException ignored) {
				send(update, "нет, дай число");
			}
		}
	}

	private String getRandom() {
		try {
			return dataService.getRandom();
		} catch (Exception e) {
			return "not found";
		}
	}

	private String getByRank(int rank) {
		try {
			return dataService.getByRank(rank);
		} catch (Exception e) {
			return "not found";
		}
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
