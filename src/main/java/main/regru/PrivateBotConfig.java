package main.regru;

import lombok.Getter;
import main.common.telegram.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PrivateBotConfig implements BotConfig {
	@Value("${regru.telegram.bot.nickname}")
	private String nickname;
	@Value("${regru.telegram.bot.token}")
	private String token;
	@Value("${regru.self.domain.name}")
	private String domainName;
}
