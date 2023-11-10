package main.common.telegram;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@SuppressWarnings("LombokGetterMayBeUsed")
@Component
public class TelegramPropsProvider implements InitializingBean {
	private static String appBaseUrl;
	private static long adminId;

	public static long getAdminId() {
		return adminId;
	}

	public static String getAppBaseUrl() {
		return appBaseUrl;
	}

	TelegramPropsProvider(
			@Value("${app.base.url}") String appBaseUrl,
			@Value("${telegram.admin.id}") long adminId
	) {
		TelegramPropsProvider.appBaseUrl = appBaseUrl + "/telegram/";
		TelegramPropsProvider.adminId = adminId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
