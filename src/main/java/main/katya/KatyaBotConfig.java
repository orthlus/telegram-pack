package main.katya;

import lombok.Getter;
import main.common.telegram.BotConfig;
import main.common.telegram.UsingPrivateApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KatyaBotConfig implements BotConfig, UsingPrivateApi {
	@Value("${katya.telegram.bot.nickname}")
	private String nickname;
	@Value("${katya.telegram.bot.token}")
	private String token;
}
