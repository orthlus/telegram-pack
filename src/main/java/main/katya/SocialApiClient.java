package main.katya;

import feign.Feign;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SocialApiClient {
	@Value("${katya.social.api.url}")
	private String url;
	private SocialApi client;

	@PostConstruct
	private void init() {
		Request.Options options = new Request.Options(2, TimeUnit.MINUTES, 2, TimeUnit.MINUTES, true);
		client = Feign.builder()
				.options(options)
				.target(SocialApi.class, url);
	}

	public ByteArrayInputStream getYouTubeFile(URI uri) {
		try(feign.Response response = client.youTubeFile(uri.toString())) {
			return new ByteArrayInputStream(response.body().asInputStream().readAllBytes());
		} catch (IOException e) {
			log.error("http error - 'YouTube download'", e);
			throw new RuntimeException("error during downloading YouTube file");
		}
	}

	public ByteArrayInputStream getTikTokFile(URI uri) {
		try(feign.Response response = client.tikTokFile(uri.toString())) {
			return new ByteArrayInputStream(response.body().asInputStream().readAllBytes());
		} catch (IOException e) {
			log.error("http error - 'tiktok download'", e);
			throw new RuntimeException("error during downloading tiktok file");
		}
	}
}
