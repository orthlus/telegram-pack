package main.billing.models.timeweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinancesResponse {
	@JsonProperty
	@Delegate
	Finances finances;

	@Getter
	public static class Finances {
		@JsonProperty("balance")
		String balance;
		@JsonProperty("currency")
		String currency;
		@JsonProperty("discount_end_date_at")
		String discountEndDateAt;
		@JsonProperty("discount_percent")
		String discountPercent;
		@JsonProperty("hourly_cost")
		String hourlyCost;
		@JsonProperty("hourly_fee")
		String hourlyFee;
		@JsonProperty("monthly_cost")
		String monthlyCost;
		@JsonProperty("monthly_fee")
		String monthlyFee;
		@JsonProperty("total_paid")
		String totalPaid;
		@JsonProperty("hours_left")
		String hoursLeft;
		@JsonProperty("autopay_card_info")
		String autopayCardInfo;
	}
}
