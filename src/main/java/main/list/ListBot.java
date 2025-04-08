package main.list;

import art.aelaort.BotName;
import art.aelaort.SpringAdminBot;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static art.aelaort.TelegramClientHelpers.execute;

@Slf4j
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
			if (update.getMessage().getText().startsWith("/add")) {
				String[] split = update.getMessage().getText().split(" ");

				if (split.length < 2) {
					send("format - /add NICKNAME NAME\ntry again");
				} else {
					String nickname = split[1];
					String name = String.join(" ", Arrays.copyOfRange(split, 2, split.length));
					String url = "/bots/%s?name={name}".formatted(nickname);
					telegramListRestTemplate.patchForObject(url, null, String.class, name);
					send("to %s set name '%s'".formatted(nickname, name));
				}
			} else {
				BotName[] arr = telegramListRestTemplate.getForObject("/bots", BotName[].class);
				List<BotName> bots = Stream.of(arr).toList();

				if (bots.isEmpty()) {
					send("bots not found");
				} else {
					String text = bots.stream()
							.filter(botName -> !botName.nickname().equals(botNickname))
							.map(ListBot::buildName)
							.collect(Collectors.joining("\n\n"));
					send(text);
				}
			}
		}
	}

	private static String buildName(BotName botName) {
		if (botName.name() == null) {
			return "@" + botName.nickname();
		}
		return "<a href=\"https://t.me/%s\">%s</a>".formatted(botName.nickname(), botName.name());
	}

	private void send(String text) {
		execute(SendMessage.builder()
						.parseMode("html")
						.text(text)
						.chatId(adminId),
				listTelegramClient);
	}
}
