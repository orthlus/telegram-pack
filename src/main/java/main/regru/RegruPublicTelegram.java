package main.regru;

import lombok.Getter;
import main.regru.common.ChatState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RegruPublicTelegram extends RegruTelegram {
	@Value("${regru.pub.telegram.bot.nickname}")
	private String nickname;
	@Value("${regru.pub.domain.name}")
	private String domainName;

	public RegruPublicTelegram(RegRuService regRuService, ChatState chatState) {
		super(regRuService, chatState);
	}
}
