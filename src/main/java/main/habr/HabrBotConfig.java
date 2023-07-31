package main.habr;

import lombok.Getter;
import main.common.telegram.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class HabrBotConfig implements BotConfig {
		@Value("${habr.telegram.bot.nickname}")
		private String nickname;
		@Value("${habr.telegram.bot.token}")
		private String token;
		@Value("${habr.telegram.channel_id}")
		private long channelId;
}
