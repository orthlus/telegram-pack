package main.payments_reminders.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import main.payments_reminders.entity.RemindToSend;

public class M {
	public record RemindCallback(
			@JsonProperty("q")
			int typeId,
			@JsonProperty("r")
			long remindId
	) implements CallbackData {
	}

	public static RemindCallback of(CallbackType callbackType, RemindToSend remind) {
		return new RemindCallback(callbackType.getId(), remind.getId());
	}

	public record RemindDaysCallback(
			@JsonProperty("q")
			int typeId,
			@JsonProperty("r")
			long remindId,
			@JsonProperty("d")
			int days
	) implements CallbackData {
	}

	public static RemindDaysCallback of(CallbackType callbackType, RemindToSend remind, int numberDays) {
		return new RemindDaysCallback(callbackType.getId(), remind.getId(), numberDays);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SomeCallback(@JsonProperty("q") int typeId) implements CallbackData {
	}

	public static SomeCallback of(CallbackType callbackType) {
		return new SomeCallback(callbackType.getId());
	}
}
