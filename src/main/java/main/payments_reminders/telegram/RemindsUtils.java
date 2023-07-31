package main.payments_reminders.telegram;

import lombok.val;
import main.payments_reminders.entity.RemindWithoutId;
import main.payments_reminders.exceptions.RemindCreateException;

import static java.lang.Integer.parseInt;

public interface RemindsUtils {
	default RemindWithoutId parseNewRemind1(String messageText) {
		val rows = messageText.split("\n");
		if (rows.length != 4) throw new RemindCreateException();

		val name = rows[0].trim();
		val startDay = parseInt(rows[1].trim());
		validDbRemindDay(startDay);
		val endDay = parseInt(rows[2].trim());
		validDbRemindDay(endDay);
		val hour = parseInt(rows[3].trim());
		validDbRemindHourOfDay(hour);

		return new RemindWithoutId(name, startDay, endDay, hour);
	}

	private void validDbRemindDay(int startDay) {
		if (startDay < 1 || startDay > 31) throw new RemindCreateException();
	}

	private void validDbRemindHourOfDay(int hourOfDay) {
		if (hourOfDay < 0 || hourOfDay > 23) throw new RemindCreateException();
	}
}
