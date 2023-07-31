package main.debts;

import lombok.RequiredArgsConstructor;
import main.Main;
import main.debts.entity.Expense;
import main.debts.entity.Income;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DebtsService {
	private final Repo repo;

	public Optional<Expense> getExpenseById(int expenseId) {
		return repo.getExpenses().stream()
				.filter(e -> e.id() == expenseId)
				.findFirst();
	}

	public Income getNextIncome(LocalDate now) {
		int today = now.getDayOfMonth();
		Set<Income> incomes = repo.getIncomes();

		// сегодня день до последнего пополнения включительно
		for (Income income : incomes)
			if (today <= income.day())
				return income;

		// сегодня день после последнего пополнения - следующее первое в месяце
		return incomes.iterator().next();
	}

	public Set<Expense> getPassedExpensesIds() {
		int today = LocalDate.now(Main.zone).getDayOfMonth();
		return repo.getExpenses().stream()
				.filter(e -> e.day() < today)
				.collect(Collectors.toSet());
	}

	public int getSumNearestExpensesBeforeNextIncome(Set<Expense> exclude) {
		LocalDate now = LocalDate.now(Main.zone);
		Income nextIncome = getNextIncome(now);
		int today = now.getDayOfMonth();
		return repo.getExpenses().stream()
				.filter(e -> !exclude.contains(e))
				.filter(e -> e.day() >= today)
				.filter(e -> e.day() <= nextIncome.day())
				.mapToInt(Expense::amount)
				.sum();
	}

	public int getSumNearestExpensesBeforeNextIncome() {
		LocalDate now = LocalDate.now(Main.zone);
		Income nextIncome = getNextIncome(now);
		int today = now.getDayOfMonth();
		return repo.getExpenses().stream()
				.filter(e -> e.day() >= today)
				.filter(e -> e.day() <= nextIncome.day())
				.mapToInt(Expense::amount)
				.sum();
	}
}
