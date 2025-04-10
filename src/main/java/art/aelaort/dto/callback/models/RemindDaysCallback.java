package art.aelaort.dto.callback.models;

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
