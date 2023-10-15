package main.common.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.common.HttpClient;
import okhttp3.FormBody;
import okhttp3.Request;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static main.common.telegram.TelegramPropsProvider.getBotApiUrl;
import static main.common.telegram.TelegramPropsProvider.getBotWebhookUrl;

@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn("main.common.telegram.telegramPropsProvider")
public class TelegramBotsWebhookRegister extends HttpClient implements InitializingBean {
	private final List<BotConfig> botConfigs;
	private final TelegramBotsRepository repo;

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, String> secrets = repo.getSecretsMap();
		for (BotConfig bot : botConfigs) {
			if (secrets.get(bot.getNickname()) == null) {
				String secret = UUID.randomUUID().toString();
				register(bot, secret);
				repo.saveSecret(bot.getNickname(), secret);
			}
		}
	}

	private void register(BotConfig bot, String secret) throws IOException {
		try {
			Request request = request(bot, getBotWebhookUrl(bot), secret);
			baseHttpClient.newCall(request).execute().body().close();
		} catch (NullPointerException ignored) {
		}
		log.info("bot {} webhook registered", bot.getNickname());
	}

	private Request request(BotConfig bot, String webhookUrl, String secret) {
		String url = "%s%s/setWebhook".formatted(getBotApiUrl(bot), bot.getToken());
		FormBody body = new FormBody.Builder()
				.add("url", webhookUrl)
				.add("secret_token", secret)
				.add("drop_pending_updates", "True")
				.build();
		return new Request.Builder().url(url).post(body).build();
	}
}
