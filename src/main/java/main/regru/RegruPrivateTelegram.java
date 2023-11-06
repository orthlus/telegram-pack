package main.regru;

import main.regru.common.ChatState;
import org.springframework.stereotype.Component;

@Component
public class RegruPrivateTelegram extends RegruTelegram {

	public RegruPrivateTelegram(PrivateBotConfig botConfig,
								RegRuService regRuService,
								ChatState chatState) {
		super(botConfig, regRuService, chatState, botConfig.getDomainName());
	}
}
