package main.billing;

import art.aelaort.Command;
import art.aelaort.SpringAdminBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
public class BillingTelegram implements SpringAdminBot {
	private final BillingService billingService;

	@AllArgsConstructor
	@Getter
	private enum Commands implements Command {
		START("/start"),
		TIMEWEB_SHOW("/timeweb_show"),
		SHOW("/show");
		final String command;
	}

	@Getter
	@Value("${billing.telegram.bot.token}")
	private String botToken;
	@Getter
	@Value("${telegram.admin.id}")
	private long adminId;
	@Qualifier("billingTelegramClient")
	private final TelegramClient telegramClient;
	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);

	@Override
	public void consumeAdmin(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();

			if (commandsMap.containsKey(messageText)) {
				handleCommand(messageText);
			} else {
				handleText(messageText);
			}
		}
	}

	private void handleText(String messageText) {
		send("команды:\n" + String.join("\n", commandsMap.keySet()));
	}

	private void handleCommand(String messageText) {
		switch (commandsMap.get(messageText)) {
			case START -> send("команды:\n" + String.join("\n", commandsMap.keySet()));
			case SHOW -> send(billingService.getAllString());
			case TIMEWEB_SHOW -> send(billingService.getByService("timeweb"));
		}
	}

	private void send(String text) {
		execute(SendMessage.builder()
				.chatId(adminId)
				.text(text)
				.parseMode("markdown")
				.build(), telegramClient);
	}
}
