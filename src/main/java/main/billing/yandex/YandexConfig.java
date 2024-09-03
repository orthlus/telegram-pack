package main.billing.yandex;

import art.aelaort.DefaultSupplierProperties;
import art.aelaort.S3Params;
import art.aelaort.SupplierProperties;
import art.aelaort.YandexIAMSupplier;
import main.billing.BillingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class YandexConfig {
	@Bean
	public YandexIAMSupplier yaIAMSupplier(RestTemplate yandexRestTemplate,
										   S3Params yandexS3Params,
										   BillingProperties billingProperties) {
		return new YandexIAMSupplier(
				yandexRestTemplate,
				yandexS3Params,
				properties(billingProperties));
	}

	private SupplierProperties properties(BillingProperties billingProperties) {
		return new DefaultSupplierProperties(
				billingProperties.getYandexSecrets().getUrl(),
				billingProperties.getYandexSecrets().getSecret(),
				billingProperties.getYandexIAMS3().getFile(),
				billingProperties.getYandexIAMS3().getBucket()
		);
	}
}
