package main.common.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/telegram")
@RequiredArgsConstructor
public class TelegramController {
	private final TelegramBotsRepository repo;
	private final List<CustomSpringWebhookBot> botClients;
	private final Map<String, String> secrets = new ConcurrentHashMap<>();

	@PostConstruct
	private void init() {
		secrets.putAll(repo.getSecretsMap());
	}

	@PostMapping("{requestPath}")
	public ResponseEntity<BotApiMethod<?>> update(@RequestBody Update update,
												  HttpServletRequest request,
												  @PathVariable String requestPath) {
		for (CustomSpringWebhookBot bot : botClients) {
			String nickname = bot.getBotUsername();

			if (nickname.equals(requestPath)) {
				log.debug("request will be processed by the {} handler", nickname);

				if (validSecret(request, requestPath)) {
					bot.onWebhookUpdateReceived(update);
				} else {
					log.info("invalid or not found secret in request for bot {}", nickname);
				}

				return ResponseEntity.ok().build();
			}
		}

		log.info("/telegram controller: request to {} - not found handlers, return 404", requestPath);
		return ResponseEntity.notFound().build();
	}

	private boolean validSecret(HttpServletRequest request, String nickname) {
		try {
			String secret = getSecret(request);
			String botStoredSecret = secrets.get(nickname);

			if (botStoredSecret == null) throw new TelegramSecretNotStoredException();
			if (!secret.equals(botStoredSecret)) throw new InvalidTelegramSecretException();

			return true;
		} catch (TelegramSecretNotFoundException e) {
			log.error("telegram request to {} without secret, skipped", nickname);
		} catch (TelegramSecretNotStoredException e) {
			log.error("failed validate telegram secret - secret not stored for bot {}", nickname);
		} catch (InvalidTelegramSecretException e) {
			log.error("telegram request to {} with invalid secret, skipped", nickname);
		}

		return false;
	}

	private String getSecret(HttpServletRequest request) {
		String secret = request.getHeader("X-Telegram-Bot-Api-Secret-Token");

		if (secret != null)
			return secret;
		else
			throw new TelegramSecretNotFoundException();
	}

	public static class TelegramSecretNotFoundException extends RuntimeException {}
	public static class TelegramSecretNotStoredException extends RuntimeException {}
	public static class InvalidTelegramSecretException extends RuntimeException {}
}
