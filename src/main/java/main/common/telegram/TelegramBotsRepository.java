package main.common.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static main.Tables.TELEGRAM_BOTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotsRepository {
	private final DSLContext db;

	public Map<String, String> getSecretsMap() {
		return db.select(TELEGRAM_BOTS.NICKNAME, TELEGRAM_BOTS.SECRET)
				.from(TELEGRAM_BOTS)
				.fetchMap(TELEGRAM_BOTS.NICKNAME, TELEGRAM_BOTS.SECRET);
	}

	public void saveSecret(String nickname, String secret) {
		db.insertInto(TELEGRAM_BOTS)
				.columns(TELEGRAM_BOTS.NICKNAME, TELEGRAM_BOTS.SECRET)
				.values(nickname, secret)
				.onDuplicateKeyUpdate()
				.set(TELEGRAM_BOTS.SECRET, secret)
				.where(TELEGRAM_BOTS.NICKNAME.eq(nickname))
				.execute();
	}
}
