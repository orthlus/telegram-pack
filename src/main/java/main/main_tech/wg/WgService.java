package main.main_tech.wg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.main_tech.wg.m.Item;
import main.main_tech.wg.m.Raw;
import main.main_tech.wg.m.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static main.Main.zone;

@Slf4j
@Component
@RequiredArgsConstructor
public class WgService {
	private final WgClient client;
	private final Repo repo;
	private String header = "name      up     down    last conn\n";
	private AtomicReference<Set<Item>> currentItemsAtomic = new AtomicReference<>(Set.of());

	private String bytes2h(long bytes) {
		return bytes < 1_000_000_000L ?
				format("%4.1f MB", bytes / (1024d * 1024)) :
				format("%4.1f GB", bytes / (1024d * 1024 * 1024));
	}

	private String secondsToStr(long seconds) {
		LocalDateTime now = now(zone);
		long nowSec = now.toEpochSecond(zone.getRules().getOffset(now));
		long x = nowSec - seconds;
		return x < 86400 ?
				format("%02d:%02d:%02d", x % 86400 / 3600, x % 3600 / 60, x % 60) :
				format("%3d days", x / 86400);
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
		Set<Item> diffByUser = getDiffByUser(currentItems, lastItems);

		ArrayList<Item> sortedDiff = new ArrayList<>(diffByUser);
		Collections.sort(sortedDiff, Comparator.comparing(o -> new BigDecimal(o.down())));
		Collections.reverse(sortedDiff);

		StringBuilder sb = new StringBuilder(header);
		for (Item item : sortedDiff) {
			String up = bytes2h(parseLong(item.up()));
			String down = bytes2h(parseLong(item.down()));
			String time = secondsToStr(parseLong(item.time()));
			String row = format("%-8s %8s %8s %9s%n", item.name(), up, down, time);
			sb.append(row);
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
						cols[2],
						cols[3],
						cols[1]
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
