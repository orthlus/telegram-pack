package main.bash;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.bash.model.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataService {
	private final ObjectMapper jacksonObjectMapper;
	@Value("${bash.file.url}")
	private URI fileUrl;
	private final TreeMap<Integer, String> quotesMap = new TreeMap<>();

	@PostConstruct
	private void init() throws Exception {
		Quote[] quotes = jacksonObjectMapper.readValue(fileUrl.toURL(), Quote[].class);
		for (Quote quote : quotes) {
			quotesMap.put(quote.getRating(), quote.getText());
		}
		log.info("quotes loaded, size - {}", quotes.length);
	}

	public List<String> getTop5() {
		Collection<String> values = quotesMap.descendingMap().values();
		Iterable<String> limit = Iterables.limit(values, 5);
		return Lists.newArrayList(limit);
	}
}
