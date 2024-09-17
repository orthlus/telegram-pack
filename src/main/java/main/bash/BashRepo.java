package main.bash;

import lombok.RequiredArgsConstructor;
import main.tables.Quotes;
import main.tables.records.QuotesRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BashRepo {
	private final DSLContext dsl;
	private final Quotes quotes = Quotes.QUOTES;

	public boolean dataExists() {
		return dsl.fetchExists(quotes);
	}

	public void saveQuotes(List<Quote> quotesList) {
		dsl.batchInsert(quotesList.stream()
						.map(quote -> new QuotesRecord(null, quote.getRating(), quote.getText()))
						.toList())
				.execute();
	}

	public String getById(int id) {
		return dsl.select(quotes.QUOTE)
				.from(quotes)
				.where(quotes.ID.eq(id))
				.fetchOne(quotes.QUOTE);
	}

	public int getCount() {
		return dsl.fetchCount(quotes);
	}
}
