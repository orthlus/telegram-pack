package main.katya;

import lombok.extern.slf4j.Slf4j;
import main.common.HttpClient;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SocialApiClient extends HttpClient {
	@Value("${katya.social.api.url}")
	private String url;
	private final OkHttpClient httpClient = baseHttpClient.newBuilder()
			.callTimeout(2, TimeUnit.MINUTES)
			.connectTimeout(2, TimeUnit.MINUTES)
			.readTimeout(2, TimeUnit.MINUTES)
			.build();

	public InputStream getYouTubeFile(URI uri) {
		Request request = new Request.Builder().get()
				.url(url + "/youtube/download?url=" + uri)
				.build();
		Call call = httpClient.newCall(request);
		try {
			Response response = call.execute();
			InputStream bodyStr = new ByteArrayInputStream(readBinaryBody(response));
			if (response.isSuccessful()) {
				return bodyStr;
			}
			log.error("http error - 'getYouTubeFile' response code - {}, body - {}", response.code(), bodyStr);
		} catch (IOException e) {
			log.error("http error - 'YouTube download'", e);
		}
		throw new RuntimeException("error during downloading YouTube file");
	}

	public InputStream getTikTokFile(URI uri) {
		Request request = new Request.Builder().get()
				.url(url + "/tiktok/download?url=" + uri)
				.build();
		Call call = httpClient.newCall(request);
		try {
			Response response = call.execute();
			InputStream bodyStr = new ByteArrayInputStream(readBinaryBody(response));
			if (response.isSuccessful()) {
				return bodyStr;
			}
			log.error("http error - 'getTikTokFile' response code - {}, body - {}", response.code(), bodyStr);
		} catch (IOException e) {
			log.error("http error - 'tiktok download'", e);
		}
		throw new RuntimeException("error during downloading tiktok file");
	}
}
