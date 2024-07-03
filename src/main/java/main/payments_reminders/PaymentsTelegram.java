package main.payments_reminders;

import art.aelaort.SpringAdminBot;
import art.aelaort.telegram.callback.CallbackType;
import art.aelaort.telegram.entity.Remind;
import art.aelaort.telegram.entity.RemindWithoutId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.common.telegram.Command;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static art.aelaort.TelegramClientHelpers.execute;
import static java.lang.Long.parseLong;
import static main.payments_reminders.UserState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentsTelegram implements SpringAdminBot {
	@Getter
	@Value("${payments.telegram.bot.token}")
	private String botToken;
	@Getter
	@Value("${telegram.admin.id}")
	private long adminId;
	@Qualifier("paymentsTelegramClient")
	private final TelegramClient telegramClient;
	private final RemindsService remindsService;
	private final CallbackDataMapper mapper;
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

	@Override
	public void consumeAdmin(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
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
		}

		switch (valueOp.get()) {
			case HOLD_ON_PAYMENT_SELECT_DAYS -> {
				Optional<Tuple2<Remind, Integer>> tupleOp = mapper.getRemindAndDaysFromCallback(callbackQuery);
				if (tupleOp.isPresent()) {
					Tuple2<Remind, Integer> t = tupleOp.get();
					try {
						remindsService.addHoldOnRemind(t.v1, t.v2);
						send("Ок");
					} catch (RuntimeException e) {
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
					delete(update);
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
					RemindWithoutId remind = remindsService.parseNewRemind(messageText);
					repo.addRemind(remind);
					state.set(NOTHING_WAIT);
					send("Ок: \n%s\n/list".formatted(remind));
				} catch (RuntimeException e) {
					send("Что-то пошло не так, попробуйте заново. \n(дата от 1 до 31, час от 0 до 23)\n/new_remind");
				}
			}
			case WAIT_DELETE_REMIND_ID -> {
				try {
					long id = parseLong(messageText.trim());
					repo.deleteRemind(id);
					state.set(NOTHING_WAIT);
					send("Ок\n/list");
				} catch (Exception e) {
					log.error("Error delete remind", e);
					send("error");
				}
			}
		}
	}

	private void handleCommand(String messageText) {
		switch (commandsMap.get(messageText)) {
			case START -> {
				state.set(NOTHING_WAIT);
				send("""
						Привет! В этого бота можно записать
						даты оплаты счетов или передачи показаний счётчиков,
						и бот напомнит о них в нужные дни.
						Команды:
						%s""".formatted(String.join("\n", commandsMap.keySet())));
			}
			case LIST -> {
				String text = repo.getReminds()
						.stream()
						.map(Remind::toString)
						.collect(Collectors.joining("\n"));
				sendInMonospace(text);
			}
			case NEW_REMIND -> {
				state.set(WAIT_NEW_REMIND_DATA);
				send("""
						Новое напоминание:
						
						имя
						день начала (1-31)
						день окончания (1-31)
						час дня (0-23)
						""");
			}
			case DELETE_REMIND -> {
				state.set(WAIT_DELETE_REMIND_ID);
				send("id для удаления платежа:");
			}
		}
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

	private void delete(Update update) {
		Long chatId;
		Integer messageId;
		if (update.hasMessage()) {
			chatId = update.getMessage().getChatId();
			messageId = update.getMessage().getMessageId();
		} else if (update.hasCallbackQuery()) {
			chatId = update.getCallbackQuery().getMessage().getChatId();
			messageId = update.getCallbackQuery().getMessage().getMessageId();
		} else {
			throw new RuntimeException("not found what delete");
		}
		execute(DeleteMessage.builder()
				.chatId(chatId)
				.messageId(messageId)
				.build(), telegramClient);
	}
}
