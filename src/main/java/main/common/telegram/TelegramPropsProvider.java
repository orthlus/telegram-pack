package main.common.telegram;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Component
public class TelegramPropsProvider implements InitializingBean {
	private static String APP_BASE_URL;
	private static long ADMIN_ID;
	private static String BOT_API_URL;

	@Value("${app.base.url}")
	private void setAppBaseUrl(String url) {
		APP_BASE_URL = url;
	}

	public static String getBotWebhookUrl(BotConfig botConfig) {
		return APP_BASE_URL + "/telegram/" + botConfig.getNickname();
	}

	@Value("${telegram.admin.id}")
	private void setAdminId(long id) {
		ADMIN_ID = id;
	}

	public static long getAdminId() {
		return ADMIN_ID;
	}

	@Value("${telegram.api.url}")
	private void setBotApiUrl(String url) {
		BOT_API_URL = url;
	}

	public static String getBotApiUrl() {
		return BOT_API_URL;
	}

	public static DefaultBotOptions getCustomBotOptions() {
		DefaultBotOptions botOptions = new DefaultBotOptions();
		botOptions.setBaseUrl(BOT_API_URL);
		return botOptions;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
