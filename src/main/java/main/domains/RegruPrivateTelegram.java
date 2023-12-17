package main.domains;

import lombok.Getter;
import main.domains.common.ChatState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RegruPrivateTelegram extends RegruTelegram {
	@Value("${domains.1.telegram.bot.nickname}")
	private String nickname;
	@Value("${domains.1.domain.name}")
	private String domainName;

	public RegruPrivateTelegram(RegRuService regRuService, ChatState chatState) {
		super(regRuService, chatState);
	}
}
