package main.common.telegram;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@SuppressWarnings("LombokGetterMayBeUsed")
@Component
public class TelegramPropsProvider implements InitializingBean {
	private static long adminId;

	public static long getAdminId() {
		return adminId;
	}

	TelegramPropsProvider(@Value("${telegram.admin.id}") long adminId) {
		TelegramPropsProvider.adminId = adminId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
