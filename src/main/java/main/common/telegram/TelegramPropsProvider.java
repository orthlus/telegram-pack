package main.common.telegram;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TelegramPropsProvider implements InitializingBean {
	private static String appBaseUrl;
	@Getter
	private static long adminId;

	TelegramPropsProvider(
			@Value("${app.base.url}") String appBaseUrl,
			@Value("${telegram.admin.id}") long adminId
	) {
		TelegramPropsProvider.appBaseUrl = appBaseUrl + "/telegram/";
		TelegramPropsProvider.adminId = adminId;
	}

	public static String getBotWebhookUrl(BotConfig botConfig) {
		return appBaseUrl + botConfig.getNickname();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
