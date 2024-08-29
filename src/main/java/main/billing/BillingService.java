package main.billing;

import lombok.RequiredArgsConstructor;
import main.billing.models.timeweb.FinancesResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("DataFlowIssue")
@Component
@RequiredArgsConstructor
public class BillingService {
	@Qualifier("timewebRestTemplate")
	private final RestTemplate timeweb;

	public String getTimewebString() {
		FinancesResponse response = timeweb.getForObject("/api/v1/account/finances", FinancesResponse.class);
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

	public String getAllString() {
		return """
				*timeweb:*
				%s"""
				.formatted(getTimewebString());
	}
}
