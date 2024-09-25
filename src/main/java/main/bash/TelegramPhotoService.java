package main.bash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramPhotoService {
	private final Map<Integer, String> fileIdsByQuoteIdToSave = new ConcurrentHashMap<>();
	private final BashRepo bashRepo;
	@Value("${telegram.admin.id}")
	private long adminId;
	@Value("${bash.external.link.prefix}")
	private String externalLinkPrefix;

//	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
	public void saveFileIds() {
		int counter = 0;
		for (Map.Entry<Integer, String> entry : fileIdsByQuoteIdToSave.entrySet()) {
			bashRepo.addFileId(entry.getKey(), entry.getValue());
			fileIdsByQuoteIdToSave.remove(entry.getKey());
			counter++;
		}
		log.info("bash photo: {} file_ids saved to db", counter);
	}

	public String getFileUrl(QuoteFile quoteFile) {
		return externalLinkPrefix + quoteFile.fileUrlId();
	}

	public void saveFileId(Integer quoteId, String photoFileId) {
		fileIdsByQuoteIdToSave.put(quoteId, photoFileId);
//		log.info("photo file_id add to queue: quoteid: {}, file_id: {}", quoteId, photoFileId);
	}

	public String getPhotoFileId(Message message) {
		return message.getPhoto()
				.stream()
				.min((o1, o2) -> o2.getWidth().compareTo(o1.getWidth()))
				.orElseThrow()
				.getFileId();
	}
}
