package main.habr;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.concurrent.TimeUnit;

public class HttpDelayInterceptor implements RequestInterceptor {
	private final int delay;

	public HttpDelayInterceptor(int delay) {
		this.delay = delay;
	}

	@Override
	public void apply(RequestTemplate template) {
		try {
			TimeUnit.SECONDS.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
