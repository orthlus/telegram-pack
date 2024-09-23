package main.bash;

import art.aelaort.SpringLongPollingBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.bash.exceptions.QuoteNotFoundException;
import main.bash.models.BashPhoto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.cached.InlineQueryResultCachedPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static art.aelaort.TelegramClientHelpers.execute;

@Component
@RequiredArgsConstructor
public class BashBot implements SpringLongPollingBot {
	private final TelegramClient bashTelegramClient;
	private final DataService dataService;
	private final TelegramPhotoService telegramPhotoService;
	private final BashPhotoProvider bashPhotoProvider;
	@Getter
	@Value("${bash.telegram.bot.token}")
	private String botToken;
	@Value("${telegram.admin.id}")
	private long adminId;

	@Override
	public void consume(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			if (messageText.startsWith("/")) {
				handleCommand(update, messageText);
			} else {
				handleText(update, messageText);
			}
		} else if (update.hasInlineQuery()) {
			handleInlineQuery(update);
		}
	}

	private void handleInlineQuery(Update update) {
		InlineQuery inlineQuery = update.getInlineQuery();
		String query = inlineQuery.getQuery();
		String inlineQueryId = inlineQuery.getId();

		Set<QuoteFile> searchResult;
		try {
			int rank = Integer.parseInt(query);
			searchResult = Set.of(getByRank(rank));
		} catch (NumberFormatException ignored) {
			searchResult = search(query);
		} catch (QuoteNotFoundException e) {
			send(update, e.getMessage());
			return;
		}


		List<InlineQueryResultCachedPhoto> resultArticles = searchResult.stream()
				.map(quoteFile -> new InlineQueryResultCachedPhoto(
						UUID.randomUUID().toString(),
						telegramPhotoService.generatePhotoFileId(bashPhotoProvider.getByQuoteFile(quoteFile))
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

	private void handleText(Update update, String messageText) {
		try {
			int rank = Integer.parseInt(messageText);
			BashPhoto bashPhoto = getPhotoByRank(rank);
			telegramPhotoService.sendPhoto(update, bashPhoto);
		} catch (NumberFormatException ignored) {
			send(update, searchOne(messageText));
		} catch (QuoteNotFoundException e) {
			send(update, e.getMessage());
		}
	}

	private void handleCommand(Update update, String messageText) {
		if (messageText.equals("/random")) {
			BashPhoto randomPhoto = getRandomPhoto();
			telegramPhotoService.sendPhoto(update, randomPhoto);
		} else if (messageText.equals("/flush")) {
			telegramPhotoService.saveFileIds();
			send(update, "ok");
		} else {
			send(update, "дай число или запрос");
		}
	}

	private Set<QuoteFile> search(String query) {
		try {
			return dataService.search(query);
		} catch (Exception e) {
			return Set.of();
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
			return dataService.getRandom().quote();
		} catch (Exception e) {
			return "not found";
		}
	}

	private QuoteFile getByRank(int rank) {
		return dataService.getByRank(rank);
	}

	private BashPhoto getPhotoByRank(int rank) {
		QuoteFile quoteFile = dataService.getByRank(rank);
		return bashPhotoProvider.getByQuoteFile(quoteFile);
	}

	private BashPhoto getRandomPhoto() {
		QuoteFile quoteFile = dataService.getRandom();
		return bashPhotoProvider.getByQuoteFile(quoteFile);
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
