package main.payments_reminders.telegram.callback;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RemindCallback(
		@JsonProperty("q")
		int typeId,
		@JsonProperty("r")
		long remindId
) implements CallbackData {
}
