package main.bash;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.bash.model.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collection;
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

	public String getByRank(int rank) {
		Collection<String> values = quotesMap.values();
		return values.stream()
				.skip(values.size() - rank)
				.findFirst()
				.orElseThrow();
	}
}
