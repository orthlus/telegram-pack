package main.billing.timeweb;

import lombok.RequiredArgsConstructor;
import main.billing.BalanceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TimewebBalance implements BalanceResponse {
	@Qualifier("timewebRestTemplate")
	private final RestTemplate restTemplate;

	@Override
	public String balanceString() {
		FinancesResponse response = restTemplate.getForObject("/api/v1/account/finances", FinancesResponse.class);
		LocalDate endDate = LocalDateTime.now().plusHours(Integer.parseInt(response.getHoursLeft())).toLocalDate();
		return """
				Баланс: %s руб
				Оплата в месяц: %s руб
				Оплачено до: %s
				Карта: %s"""
				.formatted(
						response.getBalance(),
						response.getMonthlyFee(),
						endDate,
						response.getAutopayCardInfo()
				);
	}

	@Override
	public String name() {
		return "timeweb";
	}
}
