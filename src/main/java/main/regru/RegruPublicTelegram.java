package main.regru;

import main.regru.common.ChatState;
import org.springframework.stereotype.Component;

@Component
public class RegruPublicTelegram extends RegruTelegram {

	public RegruPublicTelegram(PublicBotConfig botConfig,
							   RegRuClient regRuClient,
							   ChatState chatState) {
		super(botConfig, regRuClient, chatState, botConfig.getDomainName());
	}
}
