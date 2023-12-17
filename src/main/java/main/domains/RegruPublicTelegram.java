package main.domains;

import lombok.Getter;
import main.domains.common.ChatState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RegruPublicTelegram extends RegruTelegram {
	@Value("${domains.2.telegram.bot.nickname}")
	private String nickname;
	@Value("${domains.2.domain.name}")
	private String domainName;

	public RegruPublicTelegram(RegRuService regRuService, ChatState chatState) {
		super(regRuService, chatState);
	}
}
