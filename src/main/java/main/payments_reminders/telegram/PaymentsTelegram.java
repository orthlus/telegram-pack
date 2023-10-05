package main.payments_reminders.telegram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.common.telegram.Command;
import main.common.telegram.CustomSpringWebhookBot;
import main.payments_reminders.entity.Remind;
import main.payments_reminders.entity.RemindToSend;
import main.payments_reminders.entity.RemindWithoutId;
import main.payments_reminders.exceptions.RemindCreateException;
import main.payments_reminders.exceptions.RemindHoldOnException;
import main.payments_reminders.reminds.RemindsService;
import main.payments_reminders.reminds.Repo;
import main.payments_reminders.users.UserState;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static main.payments_reminders.users.UserState.*;

@Slf4j
@Component
public class PaymentsTelegram extends CustomSpringWebhookBot implements RemindsUtils {
	public PaymentsTelegram(PaymentsBotConfig botConfig,
							KeyboardsProvider keyboards,
							RemindsService remindsService,
							CallbackMapper mapper, Repo repo) {
		super(botConfig);
		this.keyboards = keyboards;
		this.remindsService = remindsService;
		this.mapper = mapper;
		this.repo = repo;
	}

	private final KeyboardsProvider keyboards;
	private final RemindsService remindsService;
	private final CallbackMapper mapper;
	private final Repo repo;
	private final AtomicReference<UserState> state = new AtomicReference<>(NOTHING_WAIT);

	@AllArgsConstructor
	@Getter
	private enum Commands implements Command {
		START("/start"),
		LIST("/list"),
		NEW_REMIND("/new_remind"),
		DELETE_REMIND("/delete_remind");
		final String command;
	}

	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);

	public void sendRemind(RemindToSend remind) {
		String msg = remind.getName();
		send(msg, keyboards.getRemindButtons(remind));
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
			case HOLD_ON_PAYMENT_SELECT_DAYS -> {
				Optional<Tuple2<Remind, Integer>> tupleOp = mapper.getRemindAndDaysFromCallback(callbackQuery);
				if (tupleOp.isPresent()) {
					Tuple2<Remind, Integer> t = tupleOp.get();
					try {
						remindsService.addHoldOnRemind(t.v1, t.v2);
						deleteMessage(callbackQuery);
					} catch (RemindHoldOnException e) {
						send("Что-то пошло не так, попробуйте заново");
					}
				} else {
					send("Что-то пошло не так, попробуйте заново");
				}
			}
			case SUBMIT_PAYMENT -> {
				Optional<Remind> remindOp = mapper.getRemindFromCallback(callbackQuery);
				if (remindOp.isEmpty())
					send("Что-то пошло не так, попробуйте заново");
				else {
					remindsService.submitRemind(remindOp.get());
					deleteMessage(callbackQuery);
					send("%s - завершено".formatted(remindOp.get().getName()));
				}
			}
		}
	}

	private void handleText(String messageText) {
		switch (state.get()) {
			case NOTHING_WAIT -> send("Используйте команды: \n" + String.join("\n", commandsMap.keySet()));
			case WAIT_NEW_REMIND_DATA -> {
				try {
					RemindWithoutId remind = parseNewRemind1(messageText);
					repo.addRemind(remind);
					send("Ок: \n%s\n/list".formatted(remind));
					handleCommand("/list");
					state.set(NOTHING_WAIT);
				} catch (RemindCreateException e) {
					send("Что-то пошло не так, попробуйте заново. \n(дата от 1 до 31, час от 0 до 23)");
					handleCommand("/new_remind");
				}
			}
			case WAIT_DELETE_REMIND_ID -> {
				try {
					long id = parseLong(messageText.trim());
					repo.deleteRemind(id);
					state.set(NOTHING_WAIT);
					send("Ок\n/list");
					handleCommand("/list");
				} catch (Exception e) {
					log.error("Error delete remind", e);
					send("error");
				}
			}
		}
	}

	private void handleCommand(String messageText) {
		switch (messageText) {
			case "/start" -> {
				state.set(NOTHING_WAIT);
				send("""
						Привет! В этого бота можно записать
						даты оплаты счетов или передачи показаний счётчиков,
						и бот напомнит о них в нужные дни.
						Команды:
						%s""".formatted(String.join("\n", commandsMap.keySet())));
			}
			case "/list" -> {
				String text = repo.getReminds()
						.stream()
						.map(Remind::toString)
						.collect(Collectors.joining("\n"));
				send(msg("<code>%s</code>".formatted(text)).parseMode("html"));
			}
			case "/new_remind" -> {
				state.set(WAIT_NEW_REMIND_DATA);
				send("""
						Новое напоминание:
						
						имя
						день начала (1-31)
						день окончания (1-31)
						час дня (0-23)
						""");
			}
			case "/delete_remind" -> {
				state.set(WAIT_DELETE_REMIND_ID);
				send("id для удаления платежа:");
			}
		}
	}
}
