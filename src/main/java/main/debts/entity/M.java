package main.debts.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import main.debts.callback.CallbackData;
import main.debts.callback.CallbackType;

public class M {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SomeCallback(@JsonProperty("q") int typeId) implements CallbackData {
	}

	public static SomeCallback of(CallbackType callbackType) {
		return new SomeCallback(callbackType.getId());
	}

	public record ExpenseCallback(
			@JsonProperty("q")
			int typeId,
			@JsonProperty("e")
			int expenseId
	) implements CallbackData {}

	public static ExpenseCallback of(Expense expense, CallbackType callbackType) {
		return new ExpenseCallback(callbackType.getId(), expense.id());
	}
}
