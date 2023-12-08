package main.main_tech.wg;

import lombok.RequiredArgsConstructor;
import main.main_tech.wg.data.Item;
import main.main_tech.wg.data.User;
import main.tables.WgLastStat;
import main.tables.WgUsers;
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
	private final WgUsers wu = WG_USERS;
	private final WgLastStat wls = WG_LAST_STAT;

	@Cacheable("wg-users")
	public Map<String, String> getUsers() {
		return db.select(wu.USER_KEY, wu.USER_NAME)
				.from(wu)
				.fetchMap(wu.USER_KEY, wu.USER_NAME);
	}

	@CacheEvict(value = "wg-users", allEntries = true)
	public void saveUsers(Set<User> users) {
		db.transaction(trx -> {
			trx.dsl().delete(wu).execute();

			trx.dsl().batchInsert(users.stream()
							.map(u -> new WgUsersRecord(u.key(), u.name()))
							.toList())
					.execute();
		});
	}

	public Set<Item> getLastStat() {
		return db.select(wls.USER_NAME, wls.UP, wls.DOWN, wls.TIME)
				.from(wls)
				.fetchSet(mapping(Item::new));
	}

	public void saveCurrentItems(Set<Item> items) {
		db.transaction(trx -> {
			trx.dsl().delete(wls).execute();

			trx.dsl().batchInsert(items.stream()
							.map(i -> new WgLastStatRecord(i.name(), i.up(), i.down(), i.time()))
							.toList())
					.execute();
		});
	}
}
