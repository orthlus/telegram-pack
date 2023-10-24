package main.habr.rss;

import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface RssMapper {
	default String map(ItemDto dtoObject) {
		return dtoObject.getGuid();
	}

	Set<String> map(List<ItemDto> dtoObjects);
}
