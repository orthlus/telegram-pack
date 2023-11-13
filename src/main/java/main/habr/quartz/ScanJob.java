package main.habr.quartz;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.common.QuartzJobs;
import main.habr.HabrClient;
import main.habr.HabrRepo;
import main.habr.Telegram;
import org.jooq.lambda.tuple.Tuple2;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static main.common.QuartzUtils.buildJob;

@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class ScanJob implements Job, QuartzJobs {
	private final HabrClient habrClient;
	private final Telegram telegram;
	private final HabrRepo repo;

	private void scanNewPosts() {
		Set<String> posts = habrClient.getPostsFromRss();
		Set<String> news = habrClient.getNewsFromRss();
		Set<String> lastPosts = repo.getLastRssPosts();
		Set<String> lastNews = repo.getLastRssNews();

		Sets.SetView<String> newPosts = Sets.difference(posts, lastPosts);
		Sets.SetView<String> newNews = Sets.difference(news, lastNews);

		newPosts.stream()
				.filter(habrClient::isPostHasABBR)
				.forEach(url -> telegram.sendChannelMessage(telegramMsg(url)));
		newNews.stream()
				.filter(habrClient::isPostHasABBR)
				.forEach(url -> telegram.sendChannelMessage(telegramMsg(url)));

		repo.saveLastRssPosts(posts);
		repo.saveLastRssNews(news);
	}

	private String telegramMsg(String url) {
		return "Новый пост с аббревиатурой:\n" + url;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			scanNewPosts();
		} catch (Exception e) {
			log.error("scan error", e);
			throw new JobExecutionException(e);
		}
	}

	@Override
	public List<Tuple2<JobDetail, Trigger>> getJobs() {
		return List.of(
				buildJob(ScanJob.class, "0 0 0/3 ? * *")
		);
	}
}
