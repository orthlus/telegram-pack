package main.domains;

import main.domains.common.ChatState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainTelegramClients {
	@Bean
	public DomainTelegramClient client2(
			@Value("${domains.2.telegram.bot.nickname}") String nickname,
			@Qualifier("dnsClient2") YandexDNSClient dnsClient,
			ChatState chatState
	) {
		return new DomainTelegramClient(dnsClient, chatState) {
			@Override
			public String getNickname() {
				return nickname;
			}
		};
	}
}
