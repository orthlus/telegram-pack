package main.payments_reminders.telegram.callback;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RemindDaysCallback(
		@JsonProperty("q")
		int typeId,
		@JsonProperty("r")
		long remindId,
		@JsonProperty("d")
		int days
) implements CallbackData {
}
