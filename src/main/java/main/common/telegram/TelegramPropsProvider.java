package main.common.telegram;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiConstants;

@Component
public class TelegramPropsProvider implements InitializingBean {
	private static String APP_BASE_URL;
	private static long ADMIN_ID;
	private static String BOT_API_URL;

	TelegramPropsProvider(
			@Value("${app.base.url}") String appBaseUrl,
			@Value("${telegram.admin.id}") long adminId,
			@Value("${telegram.api.url}") String botApiUrl
	) {
		APP_BASE_URL = appBaseUrl;
		ADMIN_ID = adminId;
		BOT_API_URL = botApiUrl;
	}

	public static String getBotWebhookUrl(BotConfig botConfig) {
		return APP_BASE_URL + "/telegram/" + botConfig.getNickname();
	}

	public static long getAdminId() {
		return ADMIN_ID;
	}

	public static String getBotApiUrl(BotConfig botConfig) {
		return botConfig instanceof UsingPrivateApi ? BOT_API_URL : ApiConstants.BASE_URL;
	}

	public static DefaultBotOptions getCustomBotOptions(BotConfig botConfig) {
		DefaultBotOptions botOptions = new DefaultBotOptions();
		if (botConfig instanceof UsingPrivateApi)
			botOptions.setBaseUrl(BOT_API_URL);
		return botOptions;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
