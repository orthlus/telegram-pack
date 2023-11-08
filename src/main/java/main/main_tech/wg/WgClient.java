package main.main_tech.wg;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WgClient {
	private WgHttp client;
	@Value("${main_tech.api.url}")
	private String url;
	@Value("${main_tech.api.user}")
	private String user;
	@Value("${main_tech.api.secret}")
	private String password;

	@PostConstruct
	private void init() {
		feign.Request.Options options = new feign.Request.Options(2, TimeUnit.MINUTES, 2, TimeUnit.MINUTES, true);
		client = Feign.builder()
				.options(options)
				.requestInterceptor(new BasicAuthRequestInterceptor(user, password))
				.target(WgHttp.class, url);
	}

	String getRawStat() {
		return client.raw();
	}

	String getUsers() {
		return client.users();
	}
}
