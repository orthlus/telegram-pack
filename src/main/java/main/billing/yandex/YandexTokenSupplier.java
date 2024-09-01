package main.billing.yandex;

import art.aelaort.IAMHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class YandexTokenSupplier {
	private final RestTemplate yandexRestTemplate;

	public String getToken() {
		yandexRestTemplate.postForObject(IAMHelper.functionUrl, IAMHelper.functionSecretEntity, String.class);
		// TODO read s3
		return "";
	}
}
