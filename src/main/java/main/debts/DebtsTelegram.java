package main.debts;

import art.aelaort.Command;
import art.aelaort.SpringAdminBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static art.aelaort.TelegramClientHelpers.execute;
import static main.debts.UserState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebtsTelegram implements SpringAdminBot {
	@AllArgsConstructor
	@Getter
	private enum Commands implements Command {
		EXPENSES("/expenses"),
		ADD_EXPENSE("/add_expense"),
		INCOMES("/incomes"),
		ADD_INCOME("/add_income"),
		EXPENSES_DETAILS("/expenses_details"),
		EXPENSES_FOR_DATE("/expenses_for_date"),
		DELETE_INCOME("/delete_income"),
		DELETE_EXPENSE("/delete_expense"),
		START("/start");
		final String command;
	}

	@Getter
	@Value("${debts.telegram.bot.token}")
	private String botToken;
	@Getter
	@Value("${telegram.admin.id}")
	private long adminId;
	@Qualifier("debtsTelegramClient")
	private final TelegramClient telegramClient;
	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);
	private final AtomicReference<UserState> state = new AtomicReference<>(NOTHING_WAIT);
	private final DebtsService service;

	@Override
	public void consumeAdmin(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
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
					service.addIncome(messageText);
					state.set(NOTHING_WAIT);
					send("Ок\n/incomes");
				} catch (Exception e) {
					log.error("error new income", e);
					send("Еще раз");
				}
			}
			case WAIT_NEW_EXPENSE -> {
				try {
					service.addExpense(messageText);
					state.set(NOTHING_WAIT);
					send("Ок\n/expenses");
				} catch (Exception e) {
					log.error("error new expense", e);
					send("Еще раз");
				}
			}
			case WAIT_DELETE_INCOME_ID -> {
				try {
					service.deleteIncome(messageText);
					state.set(NOTHING_WAIT);
					send("Ок\n/incomes");
				} catch (Exception e) {
					log.error("Error delete income", e);
					send("error");
				}
			}
			case WAIT_DELETE_EXPENSE_ID -> {
				try {
					service.deleteExpense(messageText);
					state.set(NOTHING_WAIT);
					send("Ок\n/expenses");
				} catch (Exception e) {
					log.error("Error delete expense", e);
					send("error");
				}
			}
			case EXPENSES_FOR_DATE_WAIT_DATE_VALUE -> {
				try {
					String text = service.getExpensesTextByDate(messageText);
					state.set(NOTHING_WAIT);
					sendInMonospace(text);
				} catch (Exception e) {
					log.error("error EXPENSES_FOR_DATE", e);
					send("Еще раз");
				}
			}
		}
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
						дата окончания\s""" + String.join(" / ", service.getDateParsePatterns()));
			}
			case DELETE_EXPENSE -> {
				state.set(WAIT_DELETE_EXPENSE_ID);
				send("id для удаления расхода:");
			}
			case INCOMES -> {
				String text = service.getIncomesText();
				sendInMonospace(text);
			}
			case EXPENSES -> {
				String text = service.getExpensesText();
				sendInCode(text);
			}
			case EXPENSES_DETAILS -> {
				service.clearExpensesCache();
				String text = service.getExpensesDetailsText();
				sendInCode(text);
			}
			case EXPENSES_FOR_DATE -> {
				state.set(EXPENSES_FOR_DATE_WAIT_DATE_VALUE);
				send("на какую дату?\n" + String.join(" / ", service.getDateParsePatterns()));
			}
		}
	}

	private void sendInCode(String text) {
		execute(SendMessage.builder()
				.chatId(adminId)
				.text("```sql\n%s\n```".formatted(text))
				.parseMode("markdown")
				.build(), telegramClient);
	}

	private void sendInMonospace(String text) {
		execute(SendMessage.builder()
				.chatId(adminId)
				.text("<code>%s</code>".formatted(text))
				.parseMode("html")
				.build(), telegramClient);
	}

	private void send(String text) {
		execute(SendMessage.builder()
				.chatId(adminId)
				.text(text)
				.build(), telegramClient);
	}
}
