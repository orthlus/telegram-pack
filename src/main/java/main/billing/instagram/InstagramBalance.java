package main.billing.instagram;

import lombok.RequiredArgsConstructor;
import main.billing.BalanceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class InstagramBalance implements BalanceResponse {
	@Qualifier("instagramRestTemplate")
	private final RestTemplate instagram;

	@Override
	public String balanceString() {
		Balance balance = instagram.getForObject("/sys/balance", Balance.class);
		return "Баланс: %.4f %s".formatted(balance.getAmount(), balance.getCurrency());
	}

	@Override
	public String name() {
		return "instagram";
	}
}
