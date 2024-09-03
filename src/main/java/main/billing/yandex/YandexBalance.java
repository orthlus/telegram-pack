package main.billing.yandex;

import art.aelaort.YandexIAMSupplier;
import lombok.RequiredArgsConstructor;
import main.billing.BalanceResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class YandexBalance implements BalanceResponse {
	private final RestTemplate yandexRestTemplate;
	private final YandexIAMSupplier yandexIAMSupplier;

	@Override
	public String balanceString() {
		double balance = request().getBalance();
		return "Баланс: %.2f руб".formatted(balance);
	}

	@Override
	public String name() {
		return "yandex";
	}

	private BillingAccounts request() {
		ResponseEntity<BillingAccounts> response = yandexRestTemplate.exchange(
				"/billing/v1/billingAccounts",
				HttpMethod.GET,
				entityBearerToken(yandexIAMSupplier.getToken()),
				BillingAccounts.class);

		return response.getBody();
	}

	private HttpEntity<?> entityBearerToken(String token) {
		return new HttpEntity<>(Map.of(AUTHORIZATION, "Bearer " + token));
	}
}
