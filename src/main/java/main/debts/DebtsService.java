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
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.rangeClosed;

@Component
@RequiredArgsConstructor
public class DebtsService {
	private final DebtsRepo repo;
	@Getter
	private final String[] dateParsePatterns = {"dd.MM.yy", "dd,MM,yy", "dd MM yy", "yyyy-MM-dd"};

	public String getExpensesTextByDate(String messageText) {
		List<Expense> expenses = repo.getExpenses(parseDate(messageText));
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
		return "%s\n==========\nИтого: %d".formatted(list, sum);
	}

	public void clearExpensesCache() {
		repo.clearExpensesCache();
	}

	public String getExpensesDetailsText() {
		List<Expense> expenses = repo.getExpenses();
		return calculateExpensesByDefault(expenses);
	}

	public String getExpensesText() {
		List<Expense> expenses = repo.getExpenses();
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

	private String calculateExpenses(List<Expense> expenses) {
		List<Income> incomes = new ArrayList<>(repo.getIncomes());

		if (incomes.size() == 2) {
			return calculateExpensesWithGroup(expenses, incomes);
		} else {
			return calculateExpensesByDefault(expenses);
		}
	}

	private String calculateExpensesWithGroup(List<Expense> expenses, List<Income> incomes) {
		Income income1 = incomes.get(0);
		Income income2 = incomes.get(1);

		int day1 = min(income1.day(), income2.day());
		int day2 = max(income1.day(), income2.day());

		List<Expense> grouped1 = filterAndGroupExpenses(expenses, getExpr(1, day1, day2));
		List<Expense> grouped2 = filterAndGroupExpenses(expenses, getExpr(2, day1, day2));
		grouped1.addAll(grouped2);

		String mainContent = grouped1.stream()
				.sorted()
				.map(Expense::toString)
				.collect(joining("\n"));

		int sumAfterDay1 = expenses.stream()
				.filter(getExpr(1, day1, day2))
				.mapToInt(Expense::amount)
				.sum();
		int sumAfterDay2 = expenses.stream()
				.filter(getExpr(2, day1, day2))
				.mapToInt(Expense::amount)
				.sum();
		String text = """
				%s
				Итого: %d
				траты с 1-ой получки - %d
				траты со 2-ой получки - %d"""
				.formatted(mainContent, sumAfterDay1 + sumAfterDay2, sumAfterDay1, sumAfterDay2);
		return appendNext3MonthExpenses(text);
	}

	private List<Expense> filterAndGroupExpenses(List<Expense> expenses, Predicate<Expense> expensePredicate) {
		Collection<Expense> list = expenses.stream()
				.filter(expensePredicate)
				.collect(toMap(Expense::name,
						Function.identity(),
						this::expenseMerge))
				.values();
		return new ArrayList<>(list);
	}

	private String calculateExpensesByDefault(List<Expense> expenses) {
		int sum = expenses.stream()
				.map(Expense::amount)
				.mapToInt(Integer::intValue)
				.sum();
		String text = expenses
				.stream()
				.map(Expense::toString)
				.collect(joining("\n"))
				+ "\n==========\n"
				+ "Итого: " + sum;
		return appendNext3MonthExpenses(text);
	}

	private String appendNext3MonthExpenses(String text) {
		int nextSixMonth = calculateSumExpensesForNext3Month();
		return text + "\nНакопить: " + formatMoneyNumber(nextSixMonth);
	}

	private Predicate<Expense> getExpr(int caseNumber, int day1, int day2) {
		return switch (caseNumber) {
			case 1 -> e -> e.day() >= day1 && e.day() < day2;
			case 2 -> e -> e.day() >= day2 || e.day() < day1;
			default -> throw new IllegalStateException("Unexpected value: " + caseNumber);
		};
	}

	private Expense expenseMerge(Expense e1, Expense e2) {
		if (!e1.name().equals(e2.name())) throw new IllegalArgumentException("expenses should be with same 'name'");

		return new Expense(e1.id(),
				e1.name(),
				e1.amount() + e2.amount(),
				min(e1.day(), e2.day()),
				Collections.max(asList(e1.expireDate(), e2.expireDate())));
	}

	private int calculateSumExpensesForNext3Month() {
		LocalDate now = LocalDate.now(Main.zone);
		Set<LocalDate> dates = rangeClosed(1, 3).mapToObj(now::plusMonths).collect(toSet());

		return dates.stream()
				.map(repo::getExpenses)
				.flatMap(List::stream)
				.mapToInt(Expense::amount)
				.sum();
	}

	private String formatMoneyNumber(int amount) {
		return "%,d".formatted(amount).replaceAll(",", " ");
	}
}
