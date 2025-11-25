package art.aelaort.payments_reminders.keyboard;

import art.aelaort.dto.callback.models.CallbackData;
import art.aelaort.dto.entity.RemindToSend;
import art.aelaort.telegram.TelegramKeyboard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static art.aelaort.dto.callback.CallbackType.HOLD_ON_PAYMENT_SELECT_DAYS;
import static art.aelaort.dto.callback.CallbackType.SUBMIT_PAYMENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeyboardsProvider implements TelegramKeyboard {
	private final CallbackTypeMapper typeMapper;
	private final ObjectMapper mapper;

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
