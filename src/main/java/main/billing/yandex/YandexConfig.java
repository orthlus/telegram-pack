package main.billing.yandex;

import art.aelaort.S3Params;
import art.aelaort.YandexIAMSupplier;
import art.aelaort.ya.func.helper.FuncParams;
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
				new FuncParams(
						billingProperties.getYandexSecrets().getSecret(),
						billingProperties.getYandexSecrets().getUrl()
				),
				billingProperties.getYandexIAMS3().getFile(),
				billingProperties.getYandexIAMS3().getBucket());
	}
}
