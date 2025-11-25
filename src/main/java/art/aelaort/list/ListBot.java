package art.aelaort.list;

import art.aelaort.telegram.BotName;
import art.aelaort.telegram.SimpleAdminBot;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static art.aelaort.telegram.client.TelegramClientHelpers.execute;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListBot implements SimpleAdminBot {
	private final TelegramClient listTelegramClient;
	private final BotsRepo botsRepo;
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

		SetMyCommands setMyCommands = SetMyCommands.builder()
				.command(new BotCommand("start", "start"))
				.command(new BotCommand("add", "add"))
				.command(new BotCommand("delete", "delete"))
				.build();
		listTelegramClient.execute(setMyCommands);
	}

	@Override
	public void consumeAdmin(Update update) {
		try {
			consumeAdmin0(update);
		} catch (Exception e) {
			log.error("list bot - some error", e);
			send("list bot - some error: " + e.getMessage());
		}
	}

	private void consumeAdmin0(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			if (update.getMessage().getText().startsWith("/add")) {
				String[] split = update.getMessage().getText().split(" ");

				if (split.length < 3) {
					send("format - /add NICKNAME NAME\ntry again");
				} else {
					String nickname = split[1];
					String name = String.join(" ", Arrays.copyOfRange(split, 2, split.length));
					botsRepo.updateBotNameByNickName(nickname, name);
					send("to %s set name '%s'".formatted(nickname, name));
				}
			} else if (update.getMessage().getText().startsWith("/delete")) {
				String[] split = update.getMessage().getText().split(" ");

				if (split.length < 2) {
					send("format - /delete NICKNAME\ntry again");
				} else {
					String nickname = split[1];
					botsRepo.deleteBotByNickname(nickname);
					send("bot %s deleted".formatted(nickname));
				}
			} else {
				Set<BotName> bots = botsRepo.getNames();
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
