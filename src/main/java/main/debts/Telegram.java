package main.debts;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.common.telegram.CustomSpringWebhookBot;
import main.debts.entity.Expense;
import main.debts.entity.Income;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
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
public class Telegram extends CustomSpringWebhookBot {
	@AllArgsConstructor
	private enum Commands {
		START("/start"),
		ADD_INCOME("/add_income"),
		DELETE_INCOME("/delete_income"),
		ADD_EXPENSE("/add_expense"),
		DELETE_EXPENSE("/delete_expense"),
		INCOMES("/incomes"),
		EXPENSES("/expenses");
		final String command;
	}

	private final Map<String, Commands> commandsMap = new HashMap<>();
	{
		for (Commands command : Commands.values()) commandsMap.put(command.command, command);
	}
	private final AtomicReference<UserState> state = new AtomicReference<>(NOTHING_WAIT);
	private final Repo repo;

	public Telegram(Config botConfig, Repo repo) {
		super(botConfig);
		this.repo = repo;
	}

	@Override
	public void onWebhookUpdate(Update update) {
		if (update.hasMessage()) {
			String messageText = update.getMessage().getText();

			if (commandsMap.containsKey(messageText)) {
				handleCommand(messageText);
			} else {
				handleText(messageText);
			}
		}
	}

	private void handleText(String messageText) {
		switch (state.get()) {
			case NOTHING_WAIT -> send("Неожиданно!");
			case WAIT_NEW_INCOME -> {
				try {
					IncomeDto data = parseIncome(messageText);
					repo.addIncome(data.amount, data.day);
					state.set(NOTHING_WAIT);
					send("Ок\n/incomes");
					handleCommand("/incomes");
				} catch (Exception e) {
					log.error("error new income", e);
					send("Еще раз");
				}
			}
			case WAIT_NEW_EXPENSE -> {
				try {
					ExpenseDto data = parseExpense(messageText);
					repo.addExpense(data.name, data.amount, data.day, data.expire);
					state.set(NOTHING_WAIT);
					send("Ок\n/expenses");
					handleCommand("/expenses");
				} catch (Exception e) {
					log.error("error new expense", e);
					send("Еще раз");
				}
			}
			case WAIT_DELETE_INCOME_ID -> {
				try {
					int id = parseInt(messageText.trim());
					repo.deleteIncome(id);
					state.set(NOTHING_WAIT);
					send("Ок\n/incomes");
					handleCommand("/incomes");
				} catch (Exception e) {
					log.error("Error delete income", e);
					send("error");
				}
			}
			case WAIT_DELETE_EXPENSE_ID -> {
				try {
					int id = parseInt(messageText.trim());
					repo.deleteExpense(id);
					state.set(NOTHING_WAIT);
					send("Ок\n/expenses");
					handleCommand("/expenses");
				} catch (Exception e) {
					log.error("Error delete expense", e);
					send("error");
				}
			}
		}
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
		String[] patterns = {"dd.MM.yy", "dd,MM,yy", "dd MM yy", "yyyy-MM-dd"};
		for (String pattern : patterns) {
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

	private void handleCommand(String messageText) {
		switch (commandsMap.get(messageText)) {
			case START -> {
				state.set(NOTHING_WAIT);
				send("Учёт доходов и расходов\n" + String.join("\n", commandsMap.keySet()));
			}
			case ADD_INCOME -> {
				state.set(WAIT_NEW_INCOME);
				send("Новый доход: сумма и день месяца\n\n1000 25");
			}
			case DELETE_INCOME -> {
				state.set(WAIT_DELETE_INCOME_ID);
				send("id для удаления дохода:");
			}
			case ADD_EXPENSE -> {
				state.set(WAIT_NEW_EXPENSE);
				send("""
						Новый расход:
						
						название
						сумма
						день месяца
						дата окончания dd.mm.yy / dd,mm,yy / dd mm yy""");
			}
			case DELETE_EXPENSE -> {
				state.set(WAIT_DELETE_EXPENSE_ID);
				send("id для удаления расхода:");
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
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
			case EXPENSES -> {
				Set<Expense> expenses = repo.getExpenses();
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
							.filter(e -> e.day() >= day2 && e.day() < day1)
							.mapToInt(Expense::amount)
							.sum();
					text += """
							Итого:
							траты с 1-ой получки - %d
							траты со 2-ой получки - %d"""
							.formatted(sumAfterDay1, sumAfterDay2);
				} else {
					text += "Итого: " + sum;
				}
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
		}
	}
}
