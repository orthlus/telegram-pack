package main.debts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.common.telegram.Command;
import main.common.telegram.DefaultWebhookBot;
import main.debts.entity.Expense;
import main.debts.entity.Income;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.joining;
import static main.debts.UserState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class Telegram implements DefaultWebhookBot {
	@AllArgsConstructor
	@Getter
	private enum Commands implements Command {
		START("/start"),
		ADD_INCOME("/add_income"),
		DELETE_INCOME("/delete_income"),
		ADD_EXPENSE("/add_expense"),
		DELETE_EXPENSE("/delete_expense"),
		INCOMES("/incomes"),
		EXPENSES("/expenses"),
		EXPENSES_FOR_DATE("/expenses_for_date");
		final String command;
	}

	@Getter
	@Value("${debts.telegram.bot.nickname}")
	private String nickname;
	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);
	private final AtomicReference<UserState> state = new AtomicReference<>(NOTHING_WAIT);
	private final Repo repo;
	private final String[] dateParsePatterns = {"dd.MM.yy", "dd,MM,yy", "dd MM yy", "yyyy-MM-dd"};

	@Override
	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (update.hasMessage()) {
			String messageText = update.getMessage().getText();

			if (commandsMap.containsKey(messageText)) {
				return handleCommand(messageText);
			} else {
				return handleText(messageText);
			}
		}
		return null;
	}

	private BotApiMethod<?> handleText(String messageText) {
		SendMessage result = null;
		switch (state.get()) {
			case NOTHING_WAIT -> result = send("Неожиданно!");
			case WAIT_NEW_INCOME -> {
				try {
					IncomeDto data = parseIncome(messageText);
					repo.addIncome(data.amount, data.day);
					state.set(NOTHING_WAIT);
					result = send("Ок\n/incomes");
				} catch (Exception e) {
					log.error("error new income", e);
					result = send("Еще раз");
				}
			}
			case WAIT_NEW_EXPENSE -> {
				try {
					ExpenseDto data = parseExpense(messageText);
					repo.addExpense(data.name, data.amount, data.day, data.expire);
					state.set(NOTHING_WAIT);
					result = send("Ок\n/expenses");
				} catch (Exception e) {
					log.error("error new expense", e);
					result = send("Еще раз");
				}
			}
			case WAIT_DELETE_INCOME_ID -> {
				try {
					int id = parseInt(messageText.trim());
					repo.deleteIncome(id);
					state.set(NOTHING_WAIT);
					result = send("Ок\n/incomes");
				} catch (Exception e) {
					log.error("Error delete income", e);
					result = send("error");
				}
			}
			case WAIT_DELETE_EXPENSE_ID -> {
				try {
					int id = parseInt(messageText.trim());
					repo.deleteExpense(id);
					state.set(NOTHING_WAIT);
					result = send("Ок\n/expenses");
				} catch (Exception e) {
					log.error("Error delete expense", e);
					result = send("error");
				}
			}
			case EXPENSES_FOR_DATE_WAIT_DATE_VALUE -> {
				try {
					Set<Expense> expenses = repo.getExpenses(parseDate(messageText));
					String text = calculateExpenses(expenses);
					state.set(NOTHING_WAIT);
					result = send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
				} catch (Exception e) {
					log.error("error EXPENSES_FOR_DATE", e);
					result = send("Еще раз");
				}
			}
		}
		return result;
	}

	record ExpenseDto(String name, int amount, int day, LocalDate expire){}
	record IncomeDto(int amount, int day){}

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

	private LocalDate parseDate(String strDate) {
		for (String pattern : dateParsePatterns) {
			try {
				return parse(strDate, ofPattern(pattern));
			} catch (IllegalArgumentException | DateTimeParseException ignored) {
			}
		}

		throw new IllegalArgumentException("invalid date " + strDate);
	}

	private IncomeDto parseIncome(String text) {
		String[] arr = text.split(" ");
		int amount = parseInt(arr[0]);
		int day = parseInt(arr[1]);

		if (amount < 0) throw new NumberFormatException();
		if (day < 0 || day > 31) throw new NumberFormatException();

		return new IncomeDto(amount, day);
	}

	private BotApiMethod<?> handleCommand(String messageText) {
		SendMessage result = null;
		switch (commandsMap.get(messageText)) {
			case START -> {
				state.set(NOTHING_WAIT);
				result = send("Учёт доходов и расходов\n" + String.join("\n", commandsMap.keySet()));
			}
			case ADD_INCOME -> {
				state.set(WAIT_NEW_INCOME);
				result = send("Новый доход: сумма и день месяца\n\n1000 25");
			}
			case DELETE_INCOME -> {
				state.set(WAIT_DELETE_INCOME_ID);
				result = send("id для удаления дохода:");
			}
			case ADD_EXPENSE -> {
				state.set(WAIT_NEW_EXPENSE);
				result = send("""
						Новый расход:
						
						название
						сумма
						день месяца
						дата окончания\s""" + String.join(" / ", dateParsePatterns));
			}
			case DELETE_EXPENSE -> {
				state.set(WAIT_DELETE_EXPENSE_ID);
				result = send("id для удаления расхода:");
			}
			case INCOMES -> {
				Set<Income> incomes = repo.getIncomes();
				int sum = incomes.stream()
						.map(Income::amount)
						.mapToInt(Integer::intValue)
						.sum();
				String list = incomes.stream()
						.map(Income::toString)
						.collect(joining("\n"));
				String text = "%s\n==========\nИтого: %d".formatted(list, sum);
				result = send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
			case EXPENSES -> {
				Set<Expense> expenses = repo.getExpenses();
				String text = calculateExpenses(expenses);
				result = send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
			case EXPENSES_FOR_DATE -> {
				state.set(EXPENSES_FOR_DATE_WAIT_DATE_VALUE);
				result = send("на какую дату?\n" + String.join(" / ", dateParsePatterns));
			}
		}
		return result;
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

		return text;
	}
}
