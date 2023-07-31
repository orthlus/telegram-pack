package main.payments_reminders.telegram;

import lombok.Getter;
import main.common.telegram.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PaymentsBotConfig implements BotConfig {
	@Value("${payments.telegram.bot.nickname}")
	private String nickname;
	@Value("${payments.telegram.bot.token}")
	private String token;
}
