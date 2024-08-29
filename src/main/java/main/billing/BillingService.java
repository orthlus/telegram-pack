package main.billing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingService {
	private final List<BalanceResponse> balances;

	public String getAllString() {
		return balances.stream()
				.map(BalanceResponse::text)
				.collect(Collectors.joining("\n\n"));
	}
}
