package main.bash;

import art.aelaort.SpringLongPollingBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.bash.exceptions.QuoteNotFoundException;
import main.bash.models.QuoteFile;
import main.bash.models.QuoteFileUrlId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultPhoto;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.cached.InlineQueryResultCachedPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.InputStream;
import java.util.*;
import java.util.stream.IntStream;

import static art.aelaort.TelegramClientHelpers.execute;

@Slf4j
@Component
@RequiredArgsConstructor
public class BashBot implements SpringLongPollingBot {
	private final TelegramClient bashTelegramClient;
	private final DataService dataService;
	private final TelegramPhotoService telegramPhotoService;
	private final ImageService imageService;
	private final BashRepo bashRepo;
	private final BashS3 bashS3;
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
			searchResult = Set.of(dataService.getByRank(rank));
		} catch (NumberFormatException ignored) {
			searchResult = search(query);
		} catch (QuoteNotFoundException e) {
			send(update, e.getMessage());
			return;
		}

		try {
			bashTelegramClient.execute(AnswerInlineQuery.builder()
					.results(searchResult.stream()
							.map(this::getPhotoQueryResult)
							.filter(Objects::nonNull)
							.toList())
					.inlineQueryId(inlineQueryId)
					.build());
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

	private InlineQueryResult getPhotoQueryResult(QuoteFile quoteFile) {
		if (quoteFile.fileId() != null) {
			return InlineQueryResultCachedPhoto.builder()
					.id(UUID.randomUUID().toString())
					.photoFileId(quoteFile.fileId())
					.build();
		} else if (quoteFile.fileUrlId() != null) {
			return InlineQueryResultPhoto.builder()
					.id(UUID.randomUUID().toString())
					.photoUrl(telegramPhotoService.getFileUrl(quoteFile))
					.thumbnailUrl(telegramPhotoService.getThumbnailLink())
					.build();
		} else {
			return null;
		}
	}

	private void handleText(Update update, String messageText) {
		try {
			int rank = Integer.parseInt(messageText);
			QuoteFile quoteFile = dataService.getByRank(rank);
			sendQuotePhoto(update, quoteFile);
		} catch (NumberFormatException ignored) {
			searchFiveAndSend(update, messageText);
		} catch (QuoteNotFoundException e) {
			send(update, e.getMessage());
		}
	}

	private void handleCommand(Update update, String messageText) {
		if (messageText.equals("/random")) {
			QuoteFile quoteFile = dataService.getRandom();
			sendQuotePhoto(update, quoteFile);
		} else if (messageText.equals("/random_10")) {
			IntStream.range(0, 10)
					.mapToObj(i -> dataService.getRandom())
					.forEach(quoteFile -> sendQuotePhoto(update, quoteFile));
		} else if (messageText.equals("/flush")) {
			telegramPhotoService.saveFileIds();
			send(update, "ok");
		} else if (messageText.startsWith("/upload")) {
			upload(update, messageText);
		} else {
			send(update, "дай число или запрос");
		}
	}

	private void upload(Update update, String messageText) {
		log.info("bash upload started: {}", messageText);
		send(update, "started upload, not uploaded - " + bashRepo.hasNoFileUrlIdCount());

		Set<QuoteFile> quotes;
		if (messageText.equals("/upload")) {
			quotes = bashRepo.getQuotesWithNullFileUrlTopN(500);
		} else {
			int n = Integer.parseInt(messageText.split(" ")[1]);
			quotes = bashRepo.getQuotesWithNullFileUrlTopN(n);
		}

		int counter = 0;
		List<QuoteFileUrlId> quoteFileUrlIds = new ArrayList<>(quotes.size());
		for (QuoteFile quote : quotes) {
			InputStream inputStream = getInputStream(quote);
			String id = UUID.randomUUID() + ".png";
			bashS3.uploadFile(inputStream, id);

			quoteFileUrlIds.add(new QuoteFileUrlId(quote.quoteId(), id));

			counter++;
		}

		bashRepo.addFileUrlIds(quoteFileUrlIds);

		send(update, "quote file_url_id uploaded: " + counter);
		log.info("bash upload finish: {}", counter);
	}

	private void searchFiveAndSend(Update update, String messageText) {
		search(messageText)
				.stream()
				.limit(5)
				.forEach(quoteFile -> sendQuotePhoto(update, quoteFile));
	}

	private Set<QuoteFile> search(String query) {
		try {
			return dataService.search(query);
		} catch (Exception e) {
			return Set.of();
		}
	}

	private void sendQuotePhoto(Update update, QuoteFile quoteFile) {
//		InputStream inputStream = getInputStream(quoteFile);
//		sendPhotoByInputStream(update, inputStream);

		String fileUrl = telegramPhotoService.getFileUrl(quoteFile);
		Message message = sendPhotoByUrlOrFileId(update, fileUrl);
		telegramPhotoService.saveFileId(quoteFile, message);
	}

	private InputStream getInputStream(QuoteFile quoteFile) {
		return imageService.buildQuotePhoto(quoteFile);
	}

	private Message sendPhotoByInputStream(Update update, InputStream photo) {
		return execute(
				SendPhoto.builder()
						.chatId(update.getMessage().getChatId())
						.photo(new InputFile(photo, UUID.randomUUID().toString())),
				bashTelegramClient
		);
	}

	private Message sendPhotoByUrlOrFileId(Update update, String urlOrFileId) {
		return execute(
				SendPhoto.builder()
						.chatId(update.getMessage().getChatId())
						.photo(new InputFile(urlOrFileId)),
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
