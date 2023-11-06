package main.regru;

import main.regru.common.ChatState;
import org.springframework.stereotype.Component;

@Component
public class RegruPublicTelegram extends RegruTelegram {

	public RegruPublicTelegram(PublicBotConfig botConfig,
							   RegRuService regRuService,
							   ChatState chatState) {
		super(botConfig, regRuService, chatState, botConfig.getDomainName());
	}
}
