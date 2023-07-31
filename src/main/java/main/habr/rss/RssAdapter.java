package main.habr.rss;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RssAdapter {
	public Set<String> convert(List<ItemDto> dtoObjects) {
		return dtoObjects.stream()
				.map(ItemDto::getGuid)
				.collect(Collectors.toSet());
	}
}
