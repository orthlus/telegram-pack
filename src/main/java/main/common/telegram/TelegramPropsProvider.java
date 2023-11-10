package main.common.telegram;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiConstants;

@Component
public class TelegramPropsProvider implements InitializingBean {
	private static String appBaseUrl;
	@Getter
	private static long adminId;
	private static String botApiUrl;

	TelegramPropsProvider(
			@Value("${app.base.url}") String appBaseUrl,
			@Value("${telegram.admin.id}") long adminId,
			@Value("${telegram.api.url}") String botApiUrl
	) {
		TelegramPropsProvider.appBaseUrl = appBaseUrl + "/telegram/";
		TelegramPropsProvider.adminId = adminId;
		TelegramPropsProvider.botApiUrl = botApiUrl;
	}

	public static String getBotWebhookUrl(BotConfig botConfig) {
		return appBaseUrl + botConfig.getNickname();
	}

	public static String getBotApiUrl(BotConfig botConfig) {
		return botConfig instanceof UsingPrivateApi ? botApiUrl : ApiConstants.BASE_URL;
	}

	public static DefaultBotOptions getCustomBotOptions(BotConfig botConfig) {
		DefaultBotOptions botOptions = new DefaultBotOptions();
		if (botConfig instanceof UsingPrivateApi)
			botOptions.setBaseUrl(botApiUrl);
		return botOptions;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
