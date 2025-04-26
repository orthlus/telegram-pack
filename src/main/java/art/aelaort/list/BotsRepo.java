package art.aelaort.list;

import art.aelaort.BotName;
import lombok.RequiredArgsConstructor;
import main.tables.BotsList;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.Set;

import static main.tables.BotsList.BOTS_LIST;
import static org.jooq.Records.mapping;

@Component
@RequiredArgsConstructor
public class BotsRepo {
	private final DSLContext db;
	private final BotsList bl = BOTS_LIST;

	public Set<BotName> getNames() {
		return db.select(bl.NAME, bl.NICKNAME)
				.from(bl)
				.fetchSet(mapping(BotName::new));
	}

	public void deleteBotByNickname(String nickname) {
		db.delete(bl).where(bl.NICKNAME.eq(nickname)).execute();
	}

	public void updateBotNameByNickName(String nickname, String name) {
		db.update(bl)
				.set(bl.NAME, name)
				.where(bl.NICKNAME.eq(nickname))
				.execute();
	}
}
