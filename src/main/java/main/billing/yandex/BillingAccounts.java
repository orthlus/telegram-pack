package main.billing.yandex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BillingAccounts {
	@JsonProperty("billingAccounts")
	Account[] accounts;

	public double getBalance() {
		try {
			return accounts[0].balance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Account {
		@JsonProperty
		Double balance;
	}
}
