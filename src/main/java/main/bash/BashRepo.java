package main.bash;

import lombok.RequiredArgsConstructor;
import main.tables.Quotes;
import main.tables.records.QuotesRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.jooq.Records.mapping;

@Component
@RequiredArgsConstructor
public class BashRepo {
	private final DSLContext dsl;
	private final Quotes quotes = Quotes.QUOTES;

	public void addFileId(Integer quoteId, String fileId) {
		dsl.update(quotes)
				.set(quotes.TELEGRAM_FILE_ID, fileId)
				.where(quotes.ID.eq(quoteId))
				.execute();
	}

	public Set<String> search(String query) {
		return dsl.select(quotes.QUOTE)
				.from(quotes)
				.where("{0} %> {1}", quotes.QUOTE, query)
				.orderBy(quotes.RATING.desc())
				.limit(50)
				.fetchSet(quotes.QUOTE);
	}

	public boolean dataExists() {
		return dsl.fetchExists(quotes);
	}

	public void saveQuotes(List<Quote> quotesList) {
		dsl.batchInsert(quotesList.stream()
						.map(quote -> new QuotesRecord(null, quote.getRating(), quote.getText(), null))
						.toList())
				.execute();
	}

	public QuoteFile getById(int id) {
		return dsl.select(quotes.ID, quotes.QUOTE, quotes.TELEGRAM_FILE_ID)
				.from(quotes)
				.orderBy(quotes.ID)
				.limit(DSL.inline(1))
				.offset(DSL.inline(id - 1))
				.fetchOne(mapping(QuoteFile::new));
	}

	public int getCount() {
		return dsl.fetchCount(quotes);
	}
}
