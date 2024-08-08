package main.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWorkerClient {
	@Qualifier("chatWorkerRestTemplate")
	private final RestTemplate chatWorker;

	public void deleteMessages(long chatId, int... messageIds) {
		try {
			for (int messageId : messageIds) {
				deleteMessage(chatId, messageId);
				TimeUnit.SECONDS.sleep(1);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteMessage(long chatId, int messageId) {
		String url = "/messages/delete?chat_id=%s&message_id=%d".formatted(chatId, messageId);
		ResponseEntity<String> response = chatWorker.getForEntity(url, String.class);
		if (!response.getStatusCode().is2xxSuccessful()) {
			log.error("delete message error {}", response.getBody());
		}
	}
}
