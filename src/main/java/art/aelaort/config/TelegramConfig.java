package art.aelaort.config;

import art.aelaort.telegram.SimpleLongPollingBot;
import art.aelaort.telegram.TelegramInit;
import art.aelaort.telegram.client.TelegramClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static art.aelaort.telegram.TelegramBots.createTelegramInit;

@Configuration
public class TelegramConfig {
	@Bean
	public TelegramClient paymentsTelegramClient(@Value("${payments.telegram.bot.token}") String token) {
		return TelegramClientBuilder.builder()
				.token(token)
				.build();
	}

	@Bean
	public TelegramClient debtsTelegramClient(@Value("${debts.telegram.bot.token}") String token) {
		return TelegramClientBuilder.builder()
				.token(token)
				.build();
	}

	@Bean
	public TelegramClient listTelegramClient(@Value("${list.telegram.bot.token}") String token) {
		return TelegramClientBuilder.builder()
				.token(token)
				.build();
	}

	@Bean
	public TelegramInit telegramInit(List<SimpleLongPollingBot> bots) {
		return createTelegramInit(bots);
	}
}
