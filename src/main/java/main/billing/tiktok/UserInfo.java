package main.billing.tiktok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
public class UserInfo {
	@JsonProperty
	private int code;
	@JsonProperty("user_data")
	private UserData userData;

	@JsonIgnoreProperties(ignoreUnknown = true)
	@NoArgsConstructor
	@Getter
	public static class UserData {
		@JsonProperty
		private double balance;
	}
}
