package main.bash;

import art.aelaort.SpringLongPollingBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static art.aelaort.TelegramClientHelpers.execute;

@Component
@RequiredArgsConstructor
public class BashBot implements SpringLongPollingBot {
	private final TelegramClient bashTelegramClient;
	private final DataService dataService;
	private final ImageService imageService;
	@Getter
	@Value("${bash.telegram.bot.token}")
	private String botToken;
	@Value("${telegram.admin.id}")
	private long adminId;

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
		String inlineQueryId = inlineQuery.getId();

		Set<String> searchResult;
		try {
			int rank = Integer.parseInt(query);
			searchResult = Set.of(getByRank(rank));
		} catch (NumberFormatException ignored) {
			searchResult = search(query);
		}

		List<InlineQueryResultArticle> resultArticles = searchResult.stream()
				.map(quote -> new InlineQueryResultArticle(
						UUID.randomUUID().toString(),
						title(quote),
						new InputTextMessageContent(quote)
				))
				.toList();

		try {
			bashTelegramClient.execute(AnswerInlineQuery.builder()
					.results(resultArticles)
					.inlineQueryId(inlineQueryId)
					.build());
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

	private String title(String quote) {
		return quote.replaceAll("\n", " ");
	}

	private void handleText(Update update) {
		String messageText = update.getMessage().getText();
		if (messageText.startsWith("/")) {
			if (messageText.equals("/random")) {
				if (update.getMessage().getChat().getId() == adminId) {
					String text = getRandom();
					send(update, text);
					sendPhoto(update, imageService.buildQuotePhoto(text));
				} else {
					send(update, getRandom());
				}
			} else {
				send(update, "дай число или запрос");
			}
		} else {
			try {
				int rank = Integer.parseInt(messageText);
				String text = getByRank(rank);
				send(update, text);
			} catch (NumberFormatException ignored) {
				send(update, searchOne(messageText));
			}
		}
	}

	private Set<String> search(String query) {
		try {
			return dataService.search(query);
		} catch (Exception e) {
			return Set.of("not found");
		}
	}

	private String searchOne(String query) {
		try {
			return dataService.searchOne(query);
		} catch (Exception e) {
			return "not found";
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

	private void sendPhoto(Update update, InputStream photo) {
		Message message = execute(
				SendPhoto.builder()
						.chatId(update.getMessage().getChatId())
						.photo(new InputFile(photo, UUID.randomUUID().toString())),
				bashTelegramClient
		);
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
