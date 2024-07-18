package main.tasks;

import art.aelaort.SpringAdminChannelBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

import static art.aelaort.TelegramClientHelpers.execute;
import static java.lang.Math.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TasksBotHandler implements SpringAdminChannelBot {
	@Getter
	@Value("${tasks.telegram.bot.token}")
	private String botToken;
	@Getter
	@Value("${telegram.admin.id}")
	private long adminId;

	@Value("${tasks.channels.main.id}")
	private long mainTasksChannelId;
	@Value("${tasks.channels.short.id}")
	private long shortTasksChannelId;

	@Qualifier("tasksTelegramClient")
	private final TelegramClient telegramClient;
	private final KeyboardsProvider keyboardsProvider;

	@Override
	public Set<Long> channelsIds() {
		return Set.of(shortTasksChannelId, mainTasksChannelId);
	}

	@Override
	public void consumeAdmin(Update update) {
		if (update.hasChannelPost() && update.getChannelPost().hasText()) {
			Long chatId = update.getChannelPost().getChatId();
			if (chatId == mainTasksChannelId) {
				mainChannel(update);
			} else if (chatId == shortTasksChannelId) {
				shortChannel(update);
			}
		}
	}

	private void shortChannel(Update update) {
		log.info("new message in short channel: {}", update.getChannelPost().getText());
	}

	private void mainChannel(Update update) {
		String messageText = update.getChannelPost().getText();
		int messageId = update.getChannelPost().getMessageId();
		String taskName = messageText.split("\n")[0];
		String shortenedTask = "%s: *%s*".formatted(buildTaskId(update), taskName);
		execute(
				SendMessage.builder()
						.chatId(shortTasksChannelId)
						.text(shortenedTask)
						.replyMarkup(keyboardsProvider.finishButton(messageId))
						.parseMode("markdown"),
				telegramClient
		);
	}

	private String buildTaskId(Update update) {
		int messageId = update.getChannelPost().getMessageId();
		String link = getMainChannelMessageId(update);
		return "[#%s](%s)".formatted(messageId, link);
	}

	private String getMainChannelMessageId(Update update) {
		int messageId = update.getChannelPost().getMessageId();
		long channelId = abs(mainTasksChannelId) % (long) pow(10, (long) log10(abs(mainTasksChannelId)));
		return "https://t.me/c/%d/%d".formatted(channelId, messageId);
	}
}
