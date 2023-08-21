package main.main_tech.wg;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WgService {
	private Set<WgStatItem> parse(String raw) {
		return Arrays
				.stream(raw.split("\n"))
				.map(row -> row.split(" "))
				.map(cols -> new WgStatItem(
						cols[0],
						cols[1],
						cols[2],
						cols[3]
				))
				.collect(Collectors.toSet());
	}

	private Set<WgStatItem> getDiffByUser(Set<WgStatItem> current, Set<WgStatItem> last) {
		Set<WgStatItem> result = new HashSet<>();

		for (WgStatItem currItem : current) {
			for (WgStatItem lastItem : last) {
				if (currItem.name().equals(lastItem.name())) {
					result.add(new WgStatItem(
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
