package main.regru;

import lombok.Getter;
import main.regru.common.ChatState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RegruPrivateTelegram extends RegruTelegram {
	@Value("${regru.telegram.bot.nickname}")
	private String nickname;
	@Value("${regru.self.domain.name}")
	private String domainName;

	public RegruPrivateTelegram(RegRuService regRuService, ChatState chatState) {
		super(regRuService, chatState);
	}
}
