package main.common.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/telegram")
@RequiredArgsConstructor
public class TelegramController {
	private final List<DefaultWebhookBot> bots;

	@PostMapping("{requestPath}")
	public ResponseEntity<BotApiMethod<?>> update(@RequestBody Update update,
												  @PathVariable String requestPath) {
		try {
			return ResponseEntity.ok(bots.stream()
					.filter(bot -> bot.getNickname().equals(requestPath))
					.findFirst()
					.orElseThrow(TelegramErrorException::new)
					.onWebhookUpdateReceived(update));
		} catch (TelegramErrorException e) {
			log.info("/telegram controller: request to {} - not found handlers, return 404", requestPath);
			return ResponseEntity.notFound().build();
		}
	}

	@ResponseStatus(NOT_FOUND)
	public static class TelegramErrorException extends RuntimeException {
	}
}
