package main.main_tech;

import lombok.Getter;
import main.common.telegram.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Config implements BotConfig {
	@Value("${main_tech.telegram.bot.token}")
	private String token;
	@Value("${main_tech.telegram.bot.nickname}")
	private String nickname;
}
