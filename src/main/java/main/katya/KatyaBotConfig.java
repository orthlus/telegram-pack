package main.katya;

import lombok.Getter;
import main.common.telegram.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KatyaBotConfig implements BotConfig {
	@Value("${katya.telegram.bot.nickname}")
	private String nickname;
	@Value("${katya.telegram.bot.token}")
	private String token;

	@Override
	public boolean isPrivateApi() {
		return true;
	}
}
