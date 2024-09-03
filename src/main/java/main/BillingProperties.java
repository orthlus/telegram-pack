package main;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

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
	private YandexSecrets yandexSecrets;

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

	@Getter
	@Setter
	public static class YandexSecrets {
		private URI url;
		private String secret;
	}
}
