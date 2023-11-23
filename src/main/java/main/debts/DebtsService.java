package main.debts;

import lombok.RequiredArgsConstructor;
import main.Main;
import main.debts.entity.Expense;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.rangeClosed;

@Component
@RequiredArgsConstructor
public class DebtsService {
	private final DebtsRepo repo;

	public int calculateSumExpensesForNextSixMonth() {
		LocalDate now = LocalDate.now(Main.zone);
		Set<LocalDate> dates = rangeClosed(1, 6).mapToObj(now::plusMonths).collect(toSet());

		return dates.stream()
				.map(repo::getExpenses)
				.flatMap(Set::stream)
				.mapToInt(Expense::amount)
				.sum();
	}

	public String formatMoneyNumber(int amount) {
		return "%,d".formatted(amount).replaceAll(",", " ");
	}
}
