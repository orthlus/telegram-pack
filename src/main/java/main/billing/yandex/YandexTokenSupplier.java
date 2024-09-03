package main.billing.yandex;

import art.aelaort.S3Params;
import lombok.RequiredArgsConstructor;
import main.BillingProperties;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;

import static art.aelaort.S3ClientProvider.client;

@Component
@RequiredArgsConstructor
public class YandexTokenSupplier {
	private final RestTemplate yandexRestTemplate;
	private final S3Params yandexS3Params;
	private final BillingProperties billingProperties;

	public String getToken() {
		yandexRestTemplate.postForObject(
				billingProperties.getYandexSecrets().getUrl(),
				new HttpEntity<>(billingProperties.getYandexSecrets().getSecret()),
				String.class
		);
		return readRemoteToken();
	}

	private String readRemoteToken() {
		try (S3Client client = client(yandexS3Params)) {
			return client.getObjectAsBytes(builder -> builder
							.key(billingProperties.getYandexIAMS3().getFile())
							.bucket(billingProperties.getYandexIAMS3().getBucket()))
					.asUtf8String();
		}
	}
}
