package main.main_tech.wg;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static main.common.OkHttpUtils.readBody;

@SuppressWarnings("KotlinInternalInJava")
@Slf4j
@Component
public class WgClient {
	private OkHttpClient httpClient = new OkHttpClient.Builder()
			.callTimeout(2, TimeUnit.MINUTES)
			.connectTimeout(2, TimeUnit.MINUTES)
			.readTimeout(2, TimeUnit.MINUTES)
			.addInterceptor(new AuthInterceptor())
			.cache(null)
			.build();
	@Value("${main_tech.api.url}")
	private String url;
	@Value("${main_tech.api.user}")
	private String user;
	@Value("${main_tech.api.secret}")
	private String password;

	public String getRawStat() {
		return req("/raw-stat");
	}

	public String getStat() {
		return req("/pretty-stat");
	}

	public String req(String path) {
		Request request = new Request.Builder().get().url(url + path).build();
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

	private class AuthInterceptor implements Interceptor {
		public Response intercept(Chain chain) throws IOException {
			return chain.proceed(chain.request()
					.newBuilder()
					.header("Authorization", Credentials.basic(user, password))
					.build());
		}
	}
}