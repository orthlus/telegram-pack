package main.main_tech;

import art.aelaort.Command;
import art.aelaort.SpringAdminBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.main_tech.docker_registry.DockerRegistryService;
import main.main_tech.ruvds.RuvdsEmailClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

import static art.aelaort.TelegramClientHelpers.execute;

@Component
@RequiredArgsConstructor
public class Telegram implements SpringAdminBot {
	private final DockerRegistryService dockerRegistryService;

	private enum Commands implements Command {
		GET_CODE,
		DOCKER_CLEAN,
	}

	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);

	@Getter
	@Value("${main_tech.telegram.bot.token}")
	private String botToken;
	@Getter
	@Value("${telegram.admin.id}")
	private long adminId;
	@Qualifier("maintechTelegramClient")
	private final TelegramClient telegramClient;
	private final RuvdsEmailClient ruvdsEmailClient;

	@Override
	public void consumeAdmin(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();

			if (commandsMap.containsKey(messageText)) {
				handleCommand(messageText);
			} else {
				handleText();
			}
		}
	}

	private void handleText() {
		send("работает. команды:\n" + String.join("\n", commandsMap.keySet()));
	}

	private void handleCommand(String messageText) {
		switch (commandsMap.get(messageText)) {
			case GET_CODE -> sendInMonospace(ruvdsEmailClient.getCode());
			case DOCKER_CLEAN -> {
				int i = dockerRegistryService.cleanNotLatest();
				send("ok, cleaned - " + i);
			}
		}
	}

	private void sendInHtml(String text) {
		execute(SendMessage.builder()
				.chatId(adminId)
				.text(text)
				.parseMode("html")
				.build(), telegramClient);
	}

	private void sendInMonospace(String text) {
		execute(SendMessage.builder()
				.chatId(adminId)
				.text("<code>%s</code>".formatted(text))
				.parseMode("html")
				.build(), telegramClient);
	}

	private void send(String text) {
		execute(SendMessage.builder()
				.chatId(adminId)
				.text(text)
				.build(), telegramClient);
	}
}
