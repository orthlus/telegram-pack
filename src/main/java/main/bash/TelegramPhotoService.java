package main.bash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.bash.models.QuoteFile;
import main.bash.models.QuoteFileId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static art.aelaort.TelegramClientHelpers.execute;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramPhotoService {
	private final Map<Integer, String> fileIdsByQuoteIdToSave = new ConcurrentHashMap<>();
	private final BashRepo bashRepo;
	private final ImageService imageService;
	private final BashS3 bashS3;
	private final TelegramClient bashTelegramClient;
	@Value("${telegram.admin.id}")
	private long adminId;
	@Value("${bash.external.link.prefix}")
	private String externalLinkPrefix;
	@Value("${bash.tech.chat.id}")
	private long techChatId;

	@Scheduled(fixedRate = 2, timeUnit = TimeUnit.HOURS)
	public void logStatus() {
		int count = bashRepo.hasNoFileIdCount();
		execute(SendMessage.builder()
						.chatId(adminId)
						.text("bash quotes without file_id: " + count),
				bashTelegramClient);
	}

	@Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES, initialDelay = 2)
	public void gettingTelegramFilesIds() {
		List<QuoteFileId> toSave = bashRepo.getQuotesWithNullFileIdTopN(20)
				.stream()
				.map(this::getQuoteFileId)
				.filter(Objects::nonNull)
				.toList();
		bashRepo.addFileIds(toSave);
		log.info("bash - saved files ids: {}", toSave.size());
	}

	private QuoteFileId getQuoteFileId(QuoteFile quote) {
		try {
			Message message = execute(SendPhoto.builder()
							.chatId(techChatId)
							.photo(new InputFile(getFileUrl(quote))),
					bashTelegramClient);
			return new QuoteFileId(quote.quoteId(), getPhotoFileId(message));
		} catch (Exception e) {
			log.error("error bash getQuoteFileId", e);
			return null;
		}
	}

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
	public void saveFileIds() {
		List<QuoteFileId> toSave = fileIdsByQuoteIdToSave.entrySet()
				.stream()
				.map(e -> new QuoteFileId(e.getKey(), e.getValue()))
				.toList();
		bashRepo.addFileIds(toSave);
		toSave.forEach(quoteFileId -> fileIdsByQuoteIdToSave.remove(quoteFileId.quoteId()));
		log.info("bash photo: {} file_ids saved to db", toSave.size());
	}

	public String getFileUrl(QuoteFile quoteFile) {
		return externalLinkPrefix + quoteFile.fileUrlId();
	}

	public void saveFileId(QuoteFile quoteFile, Message message) {
		try {
			if (quoteFile.fileId() == null) {
				saveFileId(quoteFile.quoteId(), getPhotoFileId(message));
			}
		} catch (Exception ignored) {
		}
	}

	private void saveFileId(Integer quoteId, String photoFileId) {
		fileIdsByQuoteIdToSave.put(quoteId, photoFileId);
	}

	private String getPhotoFileId(Message message) {
		return message.getPhoto()
				.stream()
				.min((o1, o2) -> o2.getWidth().compareTo(o1.getWidth()))
				.orElseThrow()
				.getFileId();
	}

	public String getThumbnailLink(QuoteFile quoteFile) {
		return externalLinkPrefix + "thumbs/" + quoteFile.fileUrlId();
	}

	public void buildAndUploadThumbnail(QuoteFile quoteFile) {
		BufferedImage srcImage = imageService.buildQuotePhotoBufferedImage(quoteFile);
		InputStream inputStream = imageService.generateThumbnail(srcImage);
		String id = "thumbs/" + quoteFile.fileUrlId();
		bashS3.uploadFile(inputStream, id);
	}
}
