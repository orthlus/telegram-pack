package main.billing.models.instagram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class Balance {
	@JsonProperty
	private String currency;
	@JsonProperty
	private double amount;
}
