package main.bash;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataService {
	private final ObjectMapper jacksonObjectMapper;
	private final BashRepo repo;
	@Value("${bash.file.url}")
	private URI fileUrl;
	private final Random random = new Random();
	private int size;

	@PostConstruct
	private void init() {
		if (!repo.dataExists()) {
			Quote[] quotes = downloadData();
			List<Quote> list = Stream.of(quotes)
					.sorted((o1, o2) -> Integer.compare(o2.getRating(), o1.getRating()))
					.toList();
			repo.saveQuotes(list);
			log.info("bash - data loaded");
		}

		size = repo.getCount();
		log.info("bash - data size: {}", size);
	}

	public String getByRank(int rank) {
		if (rank < 1) {
			return "нужен положительный номер!";
		}
		try {
			return repo.getById(rank);
		} catch (ArrayIndexOutOfBoundsException e) {
			return "столько нету";
		}
	}

	public String getRandom() {
		return getByRank(random.nextInt(1, size));
	}

	private Quote[] downloadData() {
		try {
			return jacksonObjectMapper.readValue(fileUrl.toURL(), Quote[].class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
