package main.debts;

import lombok.Getter;
import main.common.telegram.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Config implements BotConfig {
	@Value("${debts.telegram.bot.nickname}")
	private String nickname;
	@Value("${debts.telegram.bot.token}")
	private String token;
}
