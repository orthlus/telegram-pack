package main.regru;

import main.regru.common.ChatState;
import org.springframework.stereotype.Component;

@Component
public class RegruPrivateTelegram extends RegruTelegram {

	public RegruPrivateTelegram(PrivateBotConfig botConfig,
								RegRuClient regRuClient,
								ChatState chatState) {
		super(botConfig, regRuClient, chatState, botConfig.getDomainName());
	}
}
