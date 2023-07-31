package main.regru;

import lombok.Getter;
import main.common.telegram.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PublicBotConfig implements BotConfig {
	@Value("${regru.pub.telegram.bot.nickname}")
	private String nickname;
	@Value("${regru.pub.telegram.bot.token}")
	private String token;
	@Value("${regru.pub.domain.name}")
	private String domainName;
}
