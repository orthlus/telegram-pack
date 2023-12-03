package main.payments_reminders.telegram;

import lombok.RequiredArgsConstructor;
import main.common.telegram.TgKeyboard;
import main.payments_reminders.entity.RemindToSend;
import main.payments_reminders.telegram.callback.CallbackData;
import main.payments_reminders.telegram.callback.CallbackDataMapper;
import main.payments_reminders.telegram.callback.CallbackTypeMapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static main.payments_reminders.telegram.callback.CallbackType.HOLD_ON_PAYMENT_SELECT_DAYS;
import static main.payments_reminders.telegram.callback.CallbackType.SUBMIT_PAYMENT;


@Component
@RequiredArgsConstructor
public class KeyboardsProvider implements TgKeyboard {
	private final CallbackDataMapper dataMapper;
	private final CallbackTypeMapper typeMapper;

	private String toJson(CallbackData data) {
		return dataMapper.dataToJson(data);
	}

	public InlineKeyboardMarkup getRemindButtons(RemindToSend remind) {
		return inlineMarkup(
						row(btn("Через 1 день", toJson(typeMapper.map(HOLD_ON_PAYMENT_SELECT_DAYS, remind, 1)))),
						row(btn("Через 2 дня", toJson(typeMapper.map(HOLD_ON_PAYMENT_SELECT_DAYS, remind, 2)))),
						row(btn("Через 3 дня", toJson(typeMapper.map(HOLD_ON_PAYMENT_SELECT_DAYS, remind, 3)))),
						row(btn("Завершить", toJson(typeMapper.map(SUBMIT_PAYMENT, remind))))
				);
	}
}
