package main.tasks;

import art.aelaort.SpringAdminGroupBot;
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
public class TasksBotHandler implements SpringAdminGroupBot {
	@Getter
	@Value("${tasks.telegram.bot.token}")
	private String botToken;
	@Getter
	@Value("${telegram.admin.id}")
	private long adminId;

	@Value("${tasks.group.id}")
	private long groupId;
	@Value("${tasks.thread.short.id}")
	private int threadShortId;
	@Value("${tasks.thread.main.id}")
	private int threadMainId;

	@Qualifier("tasksTelegramClient")
	private final TelegramClient telegramClient;

	@Override
	public Set<Long> groupsIds() {
		return Set.of(groupId);
	}

	@Override
	public void consumeAdmin(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			long chatId = update.getMessage().getChatId();
			if (chatId == groupId) {
				int threadId = update.getMessage().getMessageThreadId();
				if (threadId == threadMainId) {
					mainThread(update);
				} else if (threadId == threadShortId) {
					shortThread(update);
				}
			}
		}
	}

	private void shortThread(Update update) {
		log.info("new message in short channel: {}", update.getChannelPost().getText());
	}

	private void mainThread(Update update) {
		String messageText = update.getMessage().getText();
		String taskName = messageText.split("\n")[0];
		String shortenedTask = "%s: *%s*".formatted(buildTaskId(update), taskName);
		execute(
				SendMessage.builder()
						.chatId(groupId)
						.messageThreadId(threadShortId)
						.text(shortenedTask)
						.parseMode("markdown"),
				telegramClient
		);
	}

	private String buildTaskId(Update update) {
		int messageId = update.getMessage().getMessageId();
		String link = getMainThreadMessageLink(update);
		return "[#%s](%s)".formatted(messageId, link);
	}

	private String getMainThreadMessageLink(Update update) {
		int messageId = update.getMessage().getMessageId();
		long cleanedGroupId = abs(groupId) % (long) pow(10, (long) log10(abs(groupId)));
		return "https://t.me/c/%d/%d/%d".formatted(cleanedGroupId, threadMainId, messageId);
	}
}
