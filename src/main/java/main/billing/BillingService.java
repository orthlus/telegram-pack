package main.billing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingService {
	private final RestTemplate billingServiceRestTemplate;

	public String getAllString() {
		return billingServiceRestTemplate.getForObject("/billing", String.class);
	}

	public String getByService(String serviceName) {
		return billingServiceRestTemplate.getForObject("/billing/" + serviceName, String.class);
	}
}
