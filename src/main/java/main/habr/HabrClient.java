package main.habr;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import main.common.HttpClient;
import main.habr.rss.RssAdapter;
import main.habr.rss.RssFeed;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HabrClient extends HttpClient {
	@Value("${habr.http.timeout}")
	private int timeout;
	@Value("${habr.http.delay}")
	private int delay;
	@Autowired
	private RssAdapter rssAdapter;

	private OkHttpClient client = baseHttpClient.newBuilder()
			.callTimeout(timeout, TimeUnit.SECONDS)
			.addInterceptor(new HttpDelayInterceptor(delay))
			.build();
	private XmlMapper xmlMapper = new XmlMapper();

	public Set<String> getNewsFromRss() {
		return getRss("https://habr.com/ru/rss/news/?limit=100");
	}

	public Set<String> getPostsFromRss() {
		return getRss("https://habr.com/ru/rss/all/?limit=100");
	}

	private Set<String> getRss(String url) {
		Request request = new Request.Builder().get().url(url).build();
		try (Response response = call(request)) {
			String text = readBody(response);
			RssFeed feed = xmlMapper.readValue(text, RssFeed.class);
			return rssAdapter.convert(feed.getPosts());
		} catch (IOException e) {
			log.error("http error - HabrClient.getRss", e);
			return Set.of();
		}
	}

	private Response call(Request request) {
		try {
			return client.newCall(request).execute();
		} catch (IOException e) {
			log.error("http error by request {} - {}", request, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public boolean isPostHasABBR(int postId) {
		return isPostHasABBR("https://habr.com/ru/post/%d/".formatted(postId));
	}

	public boolean isPostHasABBR(String url) {
		Request request = new Request.Builder().get().url(url).build();
		try (Response response = call(request)) {
			int code = response.code();
			if (code == 404 || code == 403) {
				log.debug("Page {} code {}", url, code);
				return false;
			}
			if (code != 200) {
				log.info("Page {} getting error, code {}", url, code);
				return false;
			}
			String text = readBody(response);
			return text.contains("class=\"habraabbr\"");
		}
	}
}
