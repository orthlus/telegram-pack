package main.main_tech.wg;

import lombok.RequiredArgsConstructor;
import main.main_tech.wg.m.Item;
import main.main_tech.wg.m.User;
import main.tables.records.WgLastStatRecord;
import main.tables.records.WgUsersRecord;
import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static main.Tables.WG_LAST_STAT;
import static main.Tables.WG_USERS;
import static org.jooq.Records.mapping;

@Service
@RequiredArgsConstructor
public class Repo {
	private final DSLContext db;

	@Cacheable("wg-users")
	public Map<String, String> getUsers() {
		return db.select(WG_USERS.USER_KEY, WG_USERS.USER_NAME)
				.from(WG_USERS)
				.fetchMap(WG_USERS.USER_KEY, WG_USERS.USER_NAME);
	}

	@CacheEvict("wg-users")
	public void saveUsers(Set<User> users) {
		db.transaction(trx -> {
			trx.dsl().truncate(WG_USERS).execute();

			trx.dsl().batchInsert(users.stream()
							.map(u -> new WgUsersRecord(u.key(), u.name()))
							.toList())
					.execute();
		});
	}

	public Set<Item> getLastStat() {
		return db.select(WG_LAST_STAT.USER_NAME, WG_LAST_STAT.UP, WG_LAST_STAT.DOWN, WG_LAST_STAT.TIME)
				.from(WG_LAST_STAT)
				.fetchSet(mapping(Item::new));
	}

	public void saveCurrentItems(Set<Item> items) {
		db.transaction(trx -> {
			trx.dsl().delete(WG_LAST_STAT).execute();

			trx.dsl().batchInsert(items.stream()
							.map(i -> new WgLastStatRecord(i.name(), i.up(), i.down(), i.time()))
							.toList())
					.execute();
		});
	}
}
