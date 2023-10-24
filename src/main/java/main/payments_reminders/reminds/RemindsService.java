package main.payments_reminders.reminds;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.payments_reminders.entity.Remind;
import main.payments_reminders.entity.RemindWithoutId;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;

import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static main.Main.zone;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemindsService {
	private final Repo repo;

	public void addHoldOnRemind(Remind remind, int daysForHold) {
		int days = abs(daysForHold);
		LocalDate now = LocalDate.now(zone);

		LocalDate remindEndDate;
		try {
			remindEndDate = now.withDayOfMonth(remind.getEndDayOfMonth());
		} catch (DateTimeException e) {
			remindEndDate = now.plusMonths(1)
					.withDayOfMonth(1)
					.minusDays(1);
		}

		LocalDate holdEndDate = now.plusDays(days);

		boolean a = now.getMonthValue() == holdEndDate.getMonthValue();
		boolean b = remindEndDate.isAfter(holdEndDate);

		if (a && b) {
			repo.addHoldOnRemind(remind, holdEndDate);
		} else {
			throw new RuntimeException();
		}
	}

	public void submitRemind(Remind remind) {
		LocalDate holdEndDate = LocalDate.now(zone).withDayOfMonth(1).plusMonths(1);
		try {
			repo.addHoldOnRemind(remind, holdEndDate);
		} catch (DataAccessException e) {
			repo.addRemind(remind);
			repo.addHoldOnRemind(remind, holdEndDate);
		}
	}

	public RemindWithoutId parseNewRemind(String messageText) {
		String[] rows = parseRemindText(messageText);

		String name = rows[0].trim();
		int startDay = parseDay(rows[1].trim());
		int endDay = parseDay(rows[2].trim());
		int hour = parseHour(rows[3].trim());

		return new RemindWithoutId(name, startDay, endDay, hour);
	}

	private String[] parseRemindText(String s) {
		String[] rows = s.split("\n");
		if (rows.length != 4)
			throw new RuntimeException();

		return rows;
	}

	private int parseDay(String s) {
		int day = parseInt(s);
		if (day < 1 || day > 31)
			throw new RuntimeException();

		return day;
	}

	private int parseHour(String s) {
		int hour = parseInt(s);
		if (hour < 0 || hour > 23)
			throw new RuntimeException();

		return hour;
	}
}
