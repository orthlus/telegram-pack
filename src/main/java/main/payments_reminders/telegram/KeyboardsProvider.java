package main.payments_reminders.telegram;

import lombok.RequiredArgsConstructor;
import main.common.telegram.TgKeyboard;
import main.payments_reminders.entity.RemindToSend;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static main.payments_reminders.telegram.CallbackType.HOLD_ON_PAYMENT_SELECT_DAYS;
import static main.payments_reminders.telegram.CallbackType.SUBMIT_PAYMENT;
import static main.payments_reminders.telegram.M.of;


@Component
@RequiredArgsConstructor
public class KeyboardsProvider implements TgKeyboard {
	private final CallbackMapper mapper;

	private String toJson(CallbackData data) {
		return mapper.dataToJson(data);
	}

	public InlineKeyboardMarkup getRemindButtons(RemindToSend remind) {
		return inlineMarkup(
						row(btn("Через 1 день", toJson(of(HOLD_ON_PAYMENT_SELECT_DAYS, remind, 1)))),
						row(btn("Через 2 дня", toJson(of(HOLD_ON_PAYMENT_SELECT_DAYS, remind, 2)))),
						row(btn("Через 3 дня", toJson(of(HOLD_ON_PAYMENT_SELECT_DAYS, remind, 3)))),
						row(btn("Завершить", toJson(of(SUBMIT_PAYMENT, remind))))
				);
	}
}
