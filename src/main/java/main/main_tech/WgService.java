package main.main_tech;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class WgService {
	private record WgStatItem(String name, String up, String down, String time) {}

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
}
