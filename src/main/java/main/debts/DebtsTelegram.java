package main.debts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.common.telegram.Command;
import main.common.telegram.DefaultLongPollingBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static main.debts.UserState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebtsTelegram extends DefaultLongPollingBot {
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
		EXPENSES_DETAILS("/expenses_details"),
		EXPENSES_FOR_DATE("/expenses_for_date");
		final String command;
	}

	@Getter
	@Value("${debts.telegram.bot.nickname}")
	private String nickname;
	@Getter
	@Value("${debts.telegram.bot.token}")
	private String token;
	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);
	private final AtomicReference<UserState> state = new AtomicReference<>(NOTHING_WAIT);
	private final DebtsService service;

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
		return switch (state.get()) {
			case NOTHING_WAIT -> send("Неожиданно!");
			case WAIT_NEW_INCOME -> {
				try {
					service.addIncome(messageText);
					state.set(NOTHING_WAIT);
					yield send("Ок\n/incomes");
				} catch (Exception e) {
					log.error("error new income", e);
					yield send("Еще раз");
				}
			}
			case WAIT_NEW_EXPENSE -> {
				try {
					service.addExpense(messageText);
					state.set(NOTHING_WAIT);
					yield send("Ок\n/expenses");
				} catch (Exception e) {
					log.error("error new expense", e);
					yield send("Еще раз");
				}
			}
			case WAIT_DELETE_INCOME_ID -> {
				try {
					service.deleteIncome(messageText);
					state.set(NOTHING_WAIT);
					yield send("Ок\n/incomes");
				} catch (Exception e) {
					log.error("Error delete income", e);
					yield send("error");
				}
			}
			case WAIT_DELETE_EXPENSE_ID -> {
				try {
					service.deleteExpense(messageText);
					state.set(NOTHING_WAIT);
					yield send("Ок\n/expenses");
				} catch (Exception e) {
					log.error("Error delete expense", e);
					yield send("error");
				}
			}
			case EXPENSES_FOR_DATE_WAIT_DATE_VALUE -> {
				try {
					String text = service.getExpensesTextByDate(messageText);
					state.set(NOTHING_WAIT);
					yield sendInMonospace(text);
				} catch (Exception e) {
					log.error("error EXPENSES_FOR_DATE", e);
					yield send("Еще раз");
				}
			}
		};
	}

	private BotApiMethod<?> handleCommand(String messageText) {
		return switch (commandsMap.get(messageText)) {
			case START -> {
				state.set(NOTHING_WAIT);
				yield send("Учёт доходов и расходов\n" + String.join("\n", commandsMap.keySet()));
			}
			case ADD_INCOME -> {
				state.set(WAIT_NEW_INCOME);
				yield send("Новый доход: сумма и день месяца\n\n1000 25");
			}
			case DELETE_INCOME -> {
				state.set(WAIT_DELETE_INCOME_ID);
				yield send("id для удаления дохода:");
			}
			case ADD_EXPENSE -> {
				state.set(WAIT_NEW_EXPENSE);
				yield send("""
						Новый расход:
						
						название
						сумма
						день месяца
						дата окончания\s""" + String.join(" / ", service.getDateParsePatterns()));
			}
			case DELETE_EXPENSE -> {
				state.set(WAIT_DELETE_EXPENSE_ID);
				yield send("id для удаления расхода:");
			}
			case INCOMES -> {
				String text = service.getIncomesText();
				yield sendInMonospace(text);
			}
			case EXPENSES -> {
				String text = service.getExpensesText();
				yield sendInCode(text);
			}
			case EXPENSES_DETAILS -> {
				service.clearExpensesCache();
				String text = service.getExpensesDetailsText();
				yield sendInCode(text);
			}
			case EXPENSES_FOR_DATE -> {
				state.set(EXPENSES_FOR_DATE_WAIT_DATE_VALUE);
				yield send("на какую дату?\n" + String.join(" / ", service.getDateParsePatterns()));
			}
		};
	}
}
