package main.habr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	public Set<String> getLastRssPosts() {
		return db.select(HABR_LAST_RSS_POSTS.LINK)
				.from(HABR_LAST_RSS_POSTS)
				.fetchSet(HABR_LAST_RSS_POSTS.LINK);
	}

	public Set<String> getLastRssNews() {
		return db.select(HABR_LAST_RSS_NEWS.LINK)
				.from(HABR_LAST_RSS_NEWS)
				.fetchSet(HABR_LAST_RSS_NEWS.LINK);
	}

	public void saveLastRssNews(Set<String> news) {
		db.transaction(trx -> {
			trx.dsl().truncate(HABR_LAST_RSS_NEWS).execute();

			trx.dsl().batchInsert(news.stream().map(HabrLastRssNewsRecord::new).toList())
					.execute();
		});
	}

	public void saveLastRssPosts(Set<String> posts) {
		db.transaction(trx -> {
			trx.dsl().truncate(HABR_LAST_RSS_POSTS).execute();

			trx.dsl().batchInsert(posts.stream().map(HabrLastRssPostsRecord::new).toList())
					.execute();
		});
	}
}
