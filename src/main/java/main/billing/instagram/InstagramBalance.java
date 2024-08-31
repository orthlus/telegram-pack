package main.billing.instagram;

import lombok.RequiredArgsConstructor;
import main.billing.BalanceResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class InstagramBalance implements BalanceResponse {
	private final RestTemplate instagramRestTemplate;

	@Override
	public String balanceString() {
		Balance balance = instagramRestTemplate.getForObject("/sys/balance", Balance.class);
		return "Баланс: %.4f %s".formatted(balance.getAmount(), balance.getCurrency());
	}

	@Override
	public String name() {
		return "instagram";
	}
}
