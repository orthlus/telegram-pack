package main.habr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.tables.HabrLastRssNews;
import main.tables.HabrLastRssPosts;
import main.tables.records.HabrLastRssNewsRecord;
import main.tables.records.HabrLastRssPostsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.Set;

import static main.Tables.HABR_LAST_RSS_NEWS;
import static main.Tables.HABR_LAST_RSS_POSTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class HabrRepo {
	private final DSLContext db;
	private final HabrLastRssPosts hlrp = HABR_LAST_RSS_POSTS;
	private final HabrLastRssNews hlrn = HABR_LAST_RSS_NEWS;

	public Set<String> getLastRssPosts() {
		return db.select(hlrp.LINK)
				.from(hlrp)
				.fetchSet(hlrp.LINK);
	}

	public Set<String> getLastRssNews() {
		return db.select(hlrn.LINK)
				.from(hlrn)
				.fetchSet(hlrn.LINK);
	}

	public void saveLastRssNews(Set<String> news) {
		db.transaction(trx -> {
			trx.dsl().delete(hlrn).execute();

			trx.dsl().batchInsert(news.stream().map(HabrLastRssNewsRecord::new).toList())
					.execute();
		});
	}

	public void saveLastRssPosts(Set<String> posts) {
		db.transaction(trx -> {
			trx.dsl().delete(hlrp).execute();

			trx.dsl().batchInsert(posts.stream().map(HabrLastRssPostsRecord::new).toList())
					.execute();
		});
	}
}
