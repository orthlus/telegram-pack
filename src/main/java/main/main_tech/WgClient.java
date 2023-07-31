package main.main_tech;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static main.common.OkHttpUtils.readBody;

@Slf4j
@Component
public class WgClient {
	private OkHttpClient httpClient = new OkHttpClient.Builder()
			.callTimeout(2, TimeUnit.MINUTES)
			.connectTimeout(2, TimeUnit.MINUTES)
			.readTimeout(2, TimeUnit.MINUTES)
			.build();
	@Value("${main_tech.api.url}")
	private String url;
	@Value("${main_tech.api.secret}")
	private String secret;

	public String getStat() {
		FormBody body = new FormBody.Builder()
				.add("secret", secret)
				.build();
		Request request = new Request.Builder().post(body)
				.url(url)
				.addHeader("Authorization", secret)
				.build();
		Call call = httpClient.newCall(request);
		try {
			Response response = call.execute();
			String bodyStr = readBody(response);
			if (response.isSuccessful()) {
				return bodyStr;
			}
			log.error("http error - wg stat, response code - {}, body - {}", response.code(), bodyStr);
		} catch (IOException e) {
			log.error("http error - wg stat", e);
		}
		throw new RuntimeException("error wg stat");
	}
}
