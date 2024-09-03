package main;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("billing")
public class BillingProperties {
	private String timewebUrl;
	private String timewebToken;
	private String instagramUrl;
	private String instagramToken;
	private String tiktokUrl;
	private String tiktokToken;
	private String yandexUrl;
	private YandexIAMS3 yandexIAMS3;

	@Getter
	@Setter
	public static class YandexIAMS3 {
		private String url;
		private String region;
		private String id;
		private String key;
		private String bucket;
		private String file;
	}
}
