package main.tasks;

import art.aelaort.SpringAdminChannelBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class TasksBotHandler implements SpringAdminChannelBot {
	@Getter
	@Value("${tasks.telegram.bot.token}")
	private String botToken;
	@Getter
	@Value("${telegram.admin.id}")
	private long adminId;
	@Qualifier("tasksTelegramClient")
	private final TelegramClient telegramClient;

	@Override
	public Set<Long> channelsIds() {
		return Set.of();
	}

	@Override
	public void consumeAdmin(Update update) {

	}
}
