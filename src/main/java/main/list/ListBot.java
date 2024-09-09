package main.list;

import art.aelaort.SpringAdminBot;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static art.aelaort.TelegramClientHelpers.execute;

@Component
@RequiredArgsConstructor
public class ListBot implements SpringAdminBot {
	private final RestTemplate telegramListRestTemplate;
	private final TelegramClient listTelegramClient;
	@Getter
	@Value("${list.telegram.bot.token}")
	private String botToken;
	@Getter
	@Value("${telegram.admin.id}")
	private long adminId;
	private String botNickname;

	@PostConstruct
	private void init() throws Exception {
		botNickname = listTelegramClient.execute(new GetMe()).getUserName();
	}

	@Override
	public void consumeAdmin(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String[] bots = telegramListRestTemplate.getForObject("/list", String[].class);
			String text = Stream.of(bots)
					.filter(bot -> !bot.equals(botNickname))
					.map(bot -> "@" + bot)
					.collect(Collectors.joining("\n"));

			execute(SendMessage.builder()
							.text(text)
							.chatId(adminId),
					listTelegramClient);
		}
	}
}
