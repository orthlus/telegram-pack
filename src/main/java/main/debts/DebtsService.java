package main.debts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.Main;
import main.debts.entity.Expense;
import main.debts.entity.ExpenseDto;
import main.debts.entity.Income;
import main.debts.entity.IncomeDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.rangeClosed;

@Component
@RequiredArgsConstructor
public class DebtsService {
	private final DebtsRepo repo;
	@Getter
	private final String[] dateParsePatterns = {"dd.MM.yy", "dd,MM,yy", "dd MM yy", "yyyy-MM-dd"};

	public String getExpensesTextByDate(String messageText) {
		Set<Expense> expenses = repo.getExpenses(parseDate(messageText));
		return calculateExpenses(expenses);
	}

	public String getIncomesText() {
		Set<Income> incomes = repo.getIncomes();
		int sum = incomes.stream()
				.map(Income::amount)
				.mapToInt(Integer::intValue)
				.sum();
		String list = incomes.stream()
				.map(Income::toString)
				.collect(joining("\n"));
		return  "%s\n==========\nИтого: %d".formatted(list, sum);
	}

	public String getExpensesText() {
		Set<Expense> expenses = repo.getExpenses();
		return calculateExpenses(expenses);
	}

	public void deleteIncome(String messageText) {
		int id = parseInt(messageText.trim());
		repo.deleteIncome(id);
	}

	public void deleteExpense(String messageText) {
		int id = parseInt(messageText.trim());
		repo.deleteExpense(id);
	}

	public void addIncome(String messageText) {
		IncomeDto data = parseIncome(messageText);
		repo.addIncome(data);
	}

	public void addExpense(String messageText) {
		ExpenseDto data = parseExpense(messageText);
		repo.addExpense(data);
	}

	private LocalDate parseDate(String strDate) {
		for (String pattern : dateParsePatterns) {
			try {
				return parse(strDate, ofPattern(pattern));
			} catch (IllegalArgumentException | DateTimeParseException ignored) {
			}
		}

		throw new IllegalArgumentException("invalid date " + strDate);
	}

	private ExpenseDto parseExpense(String text) {
		String[] split = text.split("\n");
		String name = split[0].trim();
		int amount = parseInt(split[1].trim());
		int day = parseInt(split[2].trim());
		LocalDate expire = parseDate(split[3].trim());

		if (amount < 0) throw new NumberFormatException();
		if (day < 0 || day > 31) throw new NumberFormatException();

		return new ExpenseDto(name, amount, day, expire);
	}

	private IncomeDto parseIncome(String text) {
		String[] arr = text.split(" ");
		int amount = parseInt(arr[0]);
		int day = parseInt(arr[1]);

		if (amount < 0) throw new NumberFormatException();
		if (day < 0 || day > 31) throw new NumberFormatException();

		return new IncomeDto(amount, day);
	}

	private String calculateExpenses(Set<Expense> expenses) {
		int sum = expenses.stream()
				.map(Expense::amount)
				.mapToInt(Integer::intValue)
				.sum();
		String list = expenses
				.stream()
				.map(Expense::toString)
				.collect(joining("\n"));
		String text = list + "\n==========\n";

		List<Income> incomes = new ArrayList<>(repo.getIncomes());
		if (incomes.size() == 2) {
			int day1 = min(incomes.get(0).day(), incomes.get(1).day());
			int day2 = max(incomes.get(0).day(), incomes.get(1).day());
			int sumAfterDay1 = expenses.stream()
					.filter(e -> e.day() >= day1 && e.day() < day2)
					.mapToInt(Expense::amount)
					.sum();
			int sumAfterDay2 = expenses.stream()
					.filter(e -> e.day() >= day2 || e.day() < day1)
					.mapToInt(Expense::amount)
					.sum();
			text += """
							Итого: %d
							траты с 1-ой получки - %d
							траты со 2-ой получки - %d"""
					.formatted(sumAfterDay1 + sumAfterDay2, sumAfterDay1, sumAfterDay2);
		} else {
			text += "Итого: " + sum;
		}
		int nextSixMonth = calculateSumExpensesForNextSixMonth();
		text += "\nНакопить: " + formatMoneyNumber(nextSixMonth);

		return text;
	}

	private int calculateSumExpensesForNextSixMonth() {
		LocalDate now = LocalDate.now(Main.zone);
		Set<LocalDate> dates = rangeClosed(1, 6).mapToObj(now::plusMonths).collect(toSet());

		return dates.stream()
				.map(repo::getExpenses)
				.flatMap(Set::stream)
				.mapToInt(Expense::amount)
				.sum();
	}

	private String formatMoneyNumber(int amount) {
		return "%,d".formatted(amount).replaceAll(",", " ");
	}
}
