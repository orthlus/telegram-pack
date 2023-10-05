package main.debts;

import lombok.extern.slf4j.Slf4j;
import main.common.telegram.CustomSpringWebhookBot;
import main.debts.callback.CallbackMapper;
import main.debts.callback.CallbackType;
import main.debts.entity.Expense;
import main.debts.entity.Income;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
	private final Set<String> commands = new LinkedHashSet<>(List.of(
			"/start",
			"/balance",
			"/add_income",
			"/delete_income",
			"/add_expense",
			"/delete_expense",
			"/incomes",
			"/expenses"));
	private final AtomicReference<UserState> state = new AtomicReference<>(NOTHING_WAIT);
	private final Repo repo;
	private final Keyboards keyboards;
	private final CallbackMapper mapper;
	private final Set<Expense> markedExpenses = ConcurrentHashMap.newKeySet();
	private final DebtsService service;

	public Telegram(Config botConfig, Repo repo, Keyboards keyboards, CallbackMapper mapper, DebtsService service) {
		super(botConfig);
		this.repo = repo;
		this.keyboards = keyboards;
		this.mapper = mapper;
		this.service = service;
	}

	@Override
	public void onWebhookUpdate(Update update) {
		if (update.hasMessage()) {
			String messageText = update.getMessage().getText();

			if (commands.contains(messageText)) {
				handleCommand(messageText);
			} else {
				handleText(messageText);
			}
		} else if (update.hasCallbackQuery()) {
			handleCallback(update);
		}
	}

	private void handleCallback(Update update) {
		CallbackQuery callbackQuery = update.getCallbackQuery();

		Optional<CallbackType> valueOp = mapper.getTypeFromQuery(callbackQuery);
		if (valueOp.isEmpty()) {
			send("Что-то пошло не так, попробуйте заново (неизвестный callback)");
			return;
		}

		switch (valueOp.get()) {
			case SELECT_EXPENSE -> {
				Optional<Expense> expenseOp = mapper.getExpenseFromQuery(callbackQuery);
				if (expenseOp.isPresent()) {
					Expense exp = expenseOp.get();
					if (!markedExpenses.remove(exp)) markedExpenses.add(exp);

					int sum = service.getSumNearestExpensesBeforeNextIncome(markedExpenses);
					updateMessageTextAndKeyboard(callbackQuery, balanceText(sum), keyboards.expenses(markedExpenses));
				}
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

	private String balanceText(int nearestExpensesSum) {
		return """
			Сумма ближайших платежей
			%d
			""".formatted(nearestExpensesSum);
	}

	private void handleCommand(String messageText) {
		switch (messageText) {
			case "/start" -> {
				state.set(NOTHING_WAIT);
				send("Учёт доходов и расходов\n" + String.join("\n", commands));
			}
			case "/balance" -> {
				markedExpenses.clear();
				markedExpenses.addAll(service.getPassedExpensesIds());
				InlineKeyboardMarkup keyboard = keyboards.expenses(markedExpenses);

				int nearestExpensesSum = service.getSumNearestExpensesBeforeNextIncome();
				send(balanceText(nearestExpensesSum), keyboard);
			}
			case "/add_income" -> {
				state.set(WAIT_NEW_INCOME);
				send("Новый доход: сумма и день месяца\n\n1000 25");
			}
			case "/delete_income" -> {
				state.set(WAIT_DELETE_INCOME_ID);
				send("id для удаления дохода:");
			}
			case "/add_expense" -> {
				state.set(WAIT_NEW_EXPENSE);
				send("""
						Новый расход:
						
						название
						сумма
						день месяца
						дата окончания dd.mm.yy / dd,mm,yy / dd mm yy""");
			}
			case "/delete_expense" -> {
				state.set(WAIT_DELETE_EXPENSE_ID);
				send("id для удаления расхода:");
			}
			case "/incomes" -> {
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
			case "/expenses" -> {
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
