package main.payments_reminders.reminds;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.payments_reminders.entity.Remind;
import main.payments_reminders.exceptions.RemindHoldOnException;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;

import static java.lang.Math.abs;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemindsService {
	private final Repo repo;

	public void addHoldOnRemind(Remind remind, int daysForHold) throws RemindHoldOnException {
		int days = abs(daysForHold);
		LocalDate now = LocalDate.now();

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
			throw new RemindHoldOnException();
		}
	}

	public void submitRemind(Remind remind) {
		LocalDate holdEndDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
		try {
			repo.addHoldOnRemind(remind, holdEndDate);
		} catch (DataAccessException e) {
			repo.addRemind(remind);
			repo.addHoldOnRemind(remind, holdEndDate);
		}
	}
}
