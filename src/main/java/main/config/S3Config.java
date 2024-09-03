package main.config;

import art.aelaort.DefaultS3Params;
import art.aelaort.S3Params;
import main.billing.BillingProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
	@Autowired
	private BillingProperties billingProperties;

	@Bean
	public S3Params yandexS3Params() {
		return new DefaultS3Params(
				billingProperties.getYandexIAMS3().getId(),
				billingProperties.getYandexIAMS3().getKey(),
				billingProperties.getYandexIAMS3().getUrl(),
				billingProperties.getYandexIAMS3().getRegion()
		);
	}
}
