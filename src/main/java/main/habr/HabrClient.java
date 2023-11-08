package main.habr;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import feign.Feign;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import lombok.extern.slf4j.Slf4j;
import main.habr.rss.RssMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mapstruct.factory.Mappers.getMapper;

@Slf4j
@Component
public class HabrClient {
	@Value("${habr.http.timeout}")
	private int timeout;
	@Value("${habr.http.delay}")
	private int delay;
	private final RssMapper rssMapper = getMapper(RssMapper.class);
	private HabrHttp client;
	private final String baseUrl = "https://habr.com/ru";

	@PostConstruct
	private void init() {
		feign.Request.Options options = new feign.Request.Options(timeout, TimeUnit.SECONDS, timeout, TimeUnit.SECONDS, true);
		client = Feign.builder()
				.options(options)
				.decoder((response, type) ->
						type.getTypeName().equals("java.lang.String") ?
								new Decoder.Default().decode(response, type) :
								new JacksonDecoder(new XmlMapper()).decode(response, type))
				.requestInterceptor(new HttpDelayInterceptor(delay))
				.target(HabrHttp.class, baseUrl);
	}

	public Set<String> getNewsFromRss() {
		return rssMapper.map(client.rss("news").getPosts());
	}

	public Set<String> getPostsFromRss() {
		return rssMapper.map(client.rss("all").getPosts());
	}

	public boolean isPostHasABBR(String url) {
		try {
			String pageContent = client.pageContent(URI.create(url));
			return pageContent.contains("class=\"habraabbr\"");
		} catch (Exception e) {
			return false;
		}
	}
}
