package main.common.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.tables.TelegramBots;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static main.Tables.TELEGRAM_BOTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotsRepository {
	private final DSLContext db;
	private final TelegramBots tb = TELEGRAM_BOTS;

	public Map<String, String> getSecretsMap() {
		return db.select(tb.NICKNAME, tb.SECRET)
				.from(tb)
				.fetchMap(tb.NICKNAME, tb.SECRET);
	}

	public void saveSecret(String nickname, String secret) {
		db.insertInto(tb)
				.columns(tb.NICKNAME, tb.SECRET)
				.values(nickname, secret)
				.onDuplicateKeyUpdate()
				.set(tb.SECRET, secret)
				.where(tb.NICKNAME.eq(nickname))
				.execute();
	}
}
