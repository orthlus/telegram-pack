package main.pg_chat;

import lombok.Getter;
import main.common.telegram.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//@Component
@Getter
public class Pg implements BotConfig {
	@Value("${pg_chat.telegram.bot.nickname}")
	private String nickname;
	@Value("${pg_chat.telegram.bot.token}")
	private String token;
}
