package main.bash;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.bash.models.QuoteFile;
import main.bash.models.QuoteFileId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramPhotoService {
	private final Map<Integer, String> fileIdsByQuoteIdToSave = new ConcurrentHashMap<>();
	private final BashRepo bashRepo;
	private final ImageService imageService;
	private final BashS3 bashS3;
	@Value("${telegram.admin.id}")
	private long adminId;
	@Value("${bash.external.link.prefix}")
	private String externalLinkPrefix;

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

	public String getThumbnailLink(String fileUrl, QuoteFile quoteFile) {
		try {
			BufferedImage image = ImageIO.read(URI.create(fileUrl).toURL());
			BufferedImage resizedImage = imageService.resizeImage(image, 150, 150);
			InputStream inputStream = imageService.getPNGInputStream(resizedImage);
			String id = "thumbs/" + quoteFile.fileUrlId();
			bashS3.uploadFile(inputStream, id);

			return externalLinkPrefix + id;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
