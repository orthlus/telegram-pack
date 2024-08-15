package main.tasks;

import art.aelaort.SpringAdminGroupBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
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
	@Value("${tasks.thread.complete.id}")
	private int threadCompleteId;

	@Qualifier("tasksTelegramClient")
	private final TelegramClient telegramClient;
	private final KeyboardProvider keyboardProvider;
	private final ChatWorkerClient chatWorkerClient;

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
		} else if (update.hasCallbackQuery()) {
			long chatId = update.getCallbackQuery().getMessage().getChatId();
			if (chatId == groupId) {
				callback(update.getCallbackQuery());
			}
		}
	}

	private void callback(CallbackQuery callbackQuery) {
		chatWorkerClient.deleteMessages(
				groupId,
				callbackQuery.getMessage().getMessageId(),
				Integer.parseInt(callbackQuery.getData())
		);
	}

	private void shortThread(Update update) {
		log.info("new message in short channel: {}", update.getMessage().getText());
	}

	private void mainThread(Update update) {
		String messageText = update.getMessage().getText();
		String taskName = messageText.split("\n")[0];
		String shortenedTask = buildShortenedText(update, taskName);

		Message shortMessage = execute(
				SendMessage.builder()
						.chatId(groupId)
						.messageThreadId(threadShortId)
						.text(shortenedTask)
						.parseMode("html"),
				telegramClient
		);
		Message completeMessage = execute(
				SendMessage.builder()
						.chatId(groupId)
						.messageThreadId(threadCompleteId)
						.text(shortenedTask)
						.replyMarkup(keyboardProvider.completeTaskButton(shortMessage.getMessageId()))
						.parseMode("html"),
				telegramClient
		);

		String newShortenedTaskText = appendShortenedTask(shortenedTask, completeMessage);
		execute(
				EditMessageText.builder()
						.chatId(groupId)
						.messageId(shortMessage.getMessageId())
						.text(newShortenedTaskText)
						.parseMode("html"),
				telegramClient);
	}

	private String buildShortenedText(Update update, String taskName) {
		int messageId = update.getMessage().getMessageId();
		String link = getMainThreadMessageLink(update);

		return "<a href=\"%s\">%s</a>: <b><a href=\"%s\">%s</a></b>".formatted(link, messageId, link, taskName);
	}

	private String appendShortenedTask(String oldText, Message completeMessage) {
		return oldText + "       <a href=\"%s\">del</a>".formatted(getCompleteThreadMessageLink(completeMessage));
	}

	private String getCompleteThreadMessageLink(Message completeMessage) {
		int messageId = completeMessage.getMessageId();
		return "https://t.me/c/%d/%d/%d".formatted(getCleanedGroupId(), threadCompleteId, messageId);
	}

	private String getMainThreadMessageLink(Update update) {
		int messageId = update.getMessage().getMessageId();
		return "https://t.me/c/%d/%d/%d".formatted(getCleanedGroupId(), threadMainId, messageId);
	}

	private long getCleanedGroupId() {
		return abs(groupId) % (long) pow(10, (long) log10(abs(groupId)));
	}
}
