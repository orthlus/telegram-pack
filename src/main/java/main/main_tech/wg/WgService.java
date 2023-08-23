package main.main_tech.wg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.main_tech.wg.m.Item;
import main.main_tech.wg.m.Raw;
import main.main_tech.wg.m.User;
import org.jvnet.hk2.annotations.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static main.Main.zone;

@Slf4j
@Service
@RequiredArgsConstructor
public class WgService {
	private final WgClient client;
	private final Repo repo;
	private String header = "name      ⬆️    ⬇️     last connect\n";
	private final BigDecimal b1kk = BigDecimal.valueOf(1024 * 1024);
	private final BigDecimal b1kkk = BigDecimal.valueOf(1024 * 1024 * 1024);
	private final BigDecimal d1kkk = BigDecimal.valueOf(1_000_000_000L);
	private AtomicReference<Set<Item>> currentItemsAtomic = new AtomicReference<>(Set.of());

	private String bytes2h(BigDecimal bytes) {
		return bytes.compareTo(d1kkk) < 0 ?
				format("%4.1f MB", bytes.divide(b1kk)) :
				format("%4.1f GB", bytes.divide(b1kkk));
	}

	private String secondsToStr(long seconds) {
		LocalDateTime now = now(zone);
		long nowSec = now.toEpochSecond(zone.getRules().getOffset(now));
		long x = nowSec - seconds;
		return x < 86400 ?
				format("%4s%02d:%02d:%02d%n", "", x % 86400 / 3600, x % 3600 / 60, x % 60) :
				format("%02dd:%02d:%02d:%02d%n", x / 86400, x % 86400 / 3600, x % 3600 / 60, x % 60);
	}

	public void saveCurrentItems() {
		repo.saveCurrentItems(currentItemsAtomic.get());
	}

	public void updateUsers() {
		repo.saveUsers(parseUsers(client.getUsers()));
	}

	public String getPrettyCurrent() {
		return client.getStat();
	}

	public String getPrettyDiff() {
		Set<Raw> currentRaw = parseRaw(client.getRawStat());
		Set<Item> currentItems = join(currentRaw, repo.getUsers());
		currentItemsAtomic.set(currentItems);

		Set<Item> lastItems = repo.getLastStat();

		StringBuilder sb = new StringBuilder(header);
		for (Item item : getDiffByUser(currentItems, lastItems)) {
			String up = bytes2h(new BigDecimal(item.up()));
			String down = bytes2h(new BigDecimal(item.down()));
			String time = secondsToStr(parseLong(item.time()));
			sb.append(item.name())
					.append(" ")
					.append(up)
					.append(" ")
					.append(down)
					.append(" ")
					.append(time)
					.append("\n");
		}

		return sb.substring(0, sb.length() - 1);
	}

	private Set<Item> join(Set<Raw> raw, Map<String, String> usersByKey) {
		Set<Item> result = new HashSet<>();
		for (Raw r : raw) {
			String name = usersByKey.get(r.key());
			Item i = new Item(name, r.up(), r.down(), r.time());
			result.add(i);
		}

		return result;
	}

	private Set<User> parseUsers(String users) {
		return Arrays
				.stream(users.split("\n"))
				.map(row -> row.split(" "))
				.filter(cols -> cols.length == 2)
				.map(cols -> new User(
						cols[0],
						cols[1]
				))
				.collect(Collectors.toSet());
	}

	private Set<Raw> parseRaw(String raw) {
		return Arrays
				.stream(raw.split("\n"))
				.map(row -> row.split(" "))
				.filter(cols -> cols.length == 4)
				.map(cols -> new Raw(
						cols[0],
						cols[3],
						cols[1],
						cols[2]
				))
				.collect(Collectors.toSet());
	}

	private Set<Item> getDiffByUser(Set<Item> current, Set<Item> last) {
		Set<Item> result = new HashSet<>();

		for (Item currItem : current) {
			for (Item lastItem : last) {
				if (currItem.name().equals(lastItem.name())) {
					result.add(new Item(
							currItem.name(),
							new BigInteger(currItem.up()).subtract(new BigInteger(lastItem.up())).toString(),
							new BigInteger(currItem.down()).subtract(new BigInteger(lastItem.down())).toString(),
							currItem.time()
					));
				}
			}
		}

		return result;
	}
}
