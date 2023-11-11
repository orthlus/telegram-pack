package main.common.telegram;

import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.form.FormEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static main.common.telegram.TelegramPropsProvider.getAppBaseUrl;
import static org.telegram.telegrambots.meta.ApiConstants.BASE_URL;

@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn("main.common.telegram.telegramPropsProvider")
public class TelegramBotsWebhookRegister implements InitializingBean {
	private final List<BotConfig> botConfigs;
	private final TelegramBotsRepository repo;

	@Override
	public void afterPropertiesSet() {
		TelegramApiHttp client = Feign.builder()
				.encoder(new FormEncoder())
				.target(TelegramApiHttp.class, BASE_URL);

		Map<String, String> secrets = repo.getSecretsMap();

		for (BotConfig bot : botConfigs) {
			if (secrets.get(bot.getNickname()) == null) {
				String secret = UUID.randomUUID().toString();

				Map<String, ?> params = params(getAppBaseUrl() + bot.getNickname(), secret, true);
				client.register(bot.getToken(), params);

				repo.saveSecret(bot.getNickname(), secret);
			}
		}
	}

	interface TelegramApiHttp {
		@RequestLine("POST /bot{token}/setWebhook")
		@Headers("Content-Type: application/x-www-form-urlencoded")
		void register(@Param("token") String token, Map<String, ?> params);
	}

	private Map<String, ?> params(String url, String secretToken, boolean dropPendingUpdates) {
		return Map.of(
				"url", url,
				"secret_token", secretToken,
				"drop_pending_updates", dropPendingUpdates
		);
	}
}
