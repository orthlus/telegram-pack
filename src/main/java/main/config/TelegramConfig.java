package main.config;

import art.aelaort.SpringLongPollingBot;
import art.aelaort.TelegramClientBuilder;
import art.aelaort.TelegramInit;
import art.aelaort.TelegramListProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static art.aelaort.TelegramBots.createTelegramInit;

@EnableConfigurationProperties(TelegramListProperties.class)
@Configuration
public class TelegramConfig {
	@Bean
	public TelegramClient billingTelegramClient(@Value("${billing.telegram.bot.token}") String token) {
		return TelegramClientBuilder.builder()
				.token(token)
				.build();
	}

	@Bean
	public TelegramClient tasksTelegramClient(@Value("${tasks.telegram.bot.token}") String token) {
		return TelegramClientBuilder.builder()
				.token(token)
				.build();
	}

	@Bean
	public TelegramClient maintechTelegramClient(@Value("${main_tech.telegram.bot.token}") String token) {
		return TelegramClientBuilder.builder()
				.token(token)
				.build();
	}

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
	public TelegramInit telegramInit(List<SpringLongPollingBot> bots,
									 TelegramListProperties telegramListProperties) {
		return createTelegramInit(bots, telegramListProperties);
	}
}
