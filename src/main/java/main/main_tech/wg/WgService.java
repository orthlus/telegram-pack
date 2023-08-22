package main.main_tech.wg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.main_tech.wg.m.User;
import main.main_tech.wg.m.Raw;
import org.jvnet.hk2.annotations.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WgService {
	private final WgClient client;

	public String getPrettyCurrent() {
		return client.getStat();
	}

	public String getPrettyDiff() {

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

	private Set<Raw> getDiffByUser(Set<Raw> current, Set<Raw> last) {
		Set<Raw> result = new HashSet<>();

		for (Raw currItem : current) {
			for (Raw lastItem : last) {
				if (currItem.key().equals(lastItem.key())) {
					result.add(new Raw(
							currItem.key(),
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
