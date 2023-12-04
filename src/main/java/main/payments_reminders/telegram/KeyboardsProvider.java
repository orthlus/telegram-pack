package main.payments_reminders.telegram;

import art.aelaort.telegram.callback.models.CallbackData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.common.telegram.TgKeyboard;
import main.payments_reminders.entity.RemindToSend;
import main.payments_reminders.telegram.callback.CallbackTypeMapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static main.payments_reminders.telegram.callback.CallbackType.HOLD_ON_PAYMENT_SELECT_DAYS;
import static main.payments_reminders.telegram.callback.CallbackType.SUBMIT_PAYMENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeyboardsProvider implements TgKeyboard {
	private final CallbackTypeMapper typeMapper;
	private final ObjectMapper mapper = new ObjectMapper();

	public String toJson(CallbackData data) {
		try {
			return mapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			log.error("Error serialize callback data {}", data, e);
			throw new RuntimeException(e);
		}
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
