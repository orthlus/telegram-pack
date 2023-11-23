package main.debts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.common.telegram.Command;
import main.common.telegram.DefaultWebhookBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static main.debts.UserState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebtsTelegram implements DefaultWebhookBot {
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
		SendMessage result = null;
		switch (state.get()) {
			case NOTHING_WAIT -> result = send("Неожиданно!");
			case WAIT_NEW_INCOME -> {
				try {
					service.addIncome(messageText);
					state.set(NOTHING_WAIT);
					result = send("Ок\n/incomes");
				} catch (Exception e) {
					log.error("error new income", e);
					result = send("Еще раз");
				}
			}
			case WAIT_NEW_EXPENSE -> {
				try {
					service.addExpense(messageText);
					state.set(NOTHING_WAIT);
					result = send("Ок\n/expenses");
				} catch (Exception e) {
					log.error("error new expense", e);
					result = send("Еще раз");
				}
			}
			case WAIT_DELETE_INCOME_ID -> {
				try {
					service.deleteIncome(messageText);
					state.set(NOTHING_WAIT);
					result = send("Ок\n/incomes");
				} catch (Exception e) {
					log.error("Error delete income", e);
					result = send("error");
				}
			}
			case WAIT_DELETE_EXPENSE_ID -> {
				try {
					service.deleteExpense(messageText);
					state.set(NOTHING_WAIT);
					result = send("Ок\n/expenses");
				} catch (Exception e) {
					log.error("Error delete expense", e);
					result = send("error");
				}
			}
			case EXPENSES_FOR_DATE_WAIT_DATE_VALUE -> {
				try {
					String text = service.getExpensesTextByDate(messageText);
					state.set(NOTHING_WAIT);
					result = sendInMonospace(text);
				} catch (Exception e) {
					log.error("error EXPENSES_FOR_DATE", e);
					result = send("Еще раз");
				}
			}
		}
		return result;
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
						дата окончания\s""" + String.join(" / ", service.getDateParsePatterns()));
			}
			case DELETE_EXPENSE -> {
				state.set(WAIT_DELETE_EXPENSE_ID);
				result = send("id для удаления расхода:");
			}
			case INCOMES -> {
				String text = service.getIncomesText();
				result = sendInMonospace(text);
			}
			case EXPENSES -> {
				String text = service.getExpensesText();
				result = sendInMonospace(text);
			}
			case EXPENSES_FOR_DATE -> {
				state.set(EXPENSES_FOR_DATE_WAIT_DATE_VALUE);
				result = send("на какую дату?\n" + String.join(" / ", service.getDateParsePatterns()));
			}
		}
		return result;
	}
}
