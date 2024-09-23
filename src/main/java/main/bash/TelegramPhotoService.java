package main.bash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.bash.models.BashPhoto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static art.aelaort.TelegramClientHelpers.execute;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramPhotoService {
	private final TelegramClient bashTelegramClient;
	private final Map<Integer, String> fileIdsByQuoteIdToSave = new ConcurrentHashMap<>();
	private final BashRepo bashRepo;

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
	public void saveFileIds() {
		for (Map.Entry<Integer, String> entry : fileIdsByQuoteIdToSave.entrySet()) {
			bashRepo.addFileId(entry.getKey(), entry.getValue());
			log.info("photo file_id saved to db, quoteid: {}, file_id: {}", entry.getKey(), entry.getValue());
			fileIdsByQuoteIdToSave.remove(entry.getKey());
		}
	}

	public void sendPhoto(Update update, BashPhoto bashPhoto) {
		if (bashPhoto.getFileId() != null) {
			sendPhotoByFileId(update, bashPhoto.getFileId());
			log.info("photo send by fileid: {}", bashPhoto);
		} else {
			Message message = sendPhotoByInputStream(update, bashPhoto.getPhotoBytes());
			log.info("photo send with bytes: {}", bashPhoto);
			String photoFileId = getPhotoFileId(message);
			fileIdsByQuoteIdToSave.put(bashPhoto.getQuoteId(), photoFileId);
			log.info("photo file_id add to queue: quoteid: {}, file_id: {}", bashPhoto.getQuoteId(), photoFileId);
		}
	}

	private void sendPhotoByFileId(Update update, String fileId) {
		execute(
				SendPhoto.builder()
						.chatId(update.getMessage().getChatId())
						.photo(new InputFile(fileId)),
				bashTelegramClient
		);
	}

	private Message sendPhotoByInputStream(Update update, InputStream photo) {
		return execute(
				SendPhoto.builder()
						.chatId(update.getMessage().getChatId())
						.photo(new InputFile(photo, UUID.randomUUID().toString())),
				bashTelegramClient
		);
	}

	private String getPhotoFileId(Message message) {
		return message.getPhoto()
				.stream()
				.min((o1, o2) -> o2.getWidth().compareTo(o1.getWidth()))
				.orElseThrow()
				.getFileId();
	}
}
