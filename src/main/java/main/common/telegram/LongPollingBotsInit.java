package main.common.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LongPollingBotsInit implements InitializingBean {
	private final List<DefaultLongPollingBot> bots;

	@Override
	public void afterPropertiesSet() throws Exception {
		TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
		for (DefaultLongPollingBot bot : bots) {
			api.registerBot(bot);
			log.info("bot {} registered", bot.getNickname());
		}
	}
}
