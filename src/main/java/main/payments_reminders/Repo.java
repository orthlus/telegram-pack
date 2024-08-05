package main.payments_reminders;

import art.aelaort.telegram.entity.Remind;
import art.aelaort.telegram.entity.RemindWithoutId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.tables.PaymentsHoldsOnReminders;
import main.tables.PaymentsReminders;
import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

import static main.Tables.PAYMENTS_HOLDS_ON_REMINDERS;
import static main.Tables.PAYMENTS_REMINDERS;
import static org.jooq.Records.mapping;

@Slf4j
@Component
@RequiredArgsConstructor
public class Repo {
	private final DSLContext db;
	private final PaymentsHoldsOnReminders phor = PAYMENTS_HOLDS_ON_REMINDERS;
	private final PaymentsReminders pr = PAYMENTS_REMINDERS;

	public void addHoldOnRemind(Remind remind, LocalDate holdEndDate) {
		db.insertInto(phor)
				.columns(phor.REMIND_ID, phor.HOLD_END_DT)
				.values(remind.getId(), holdEndDate)
				.onConflict(phor.REMIND_ID)
				.doUpdate()
				.set(phor.HOLD_END_DT, holdEndDate)
				.where(phor.REMIND_ID.eq(remind.getId()))
				.execute();
	}

	public void addRemind(RemindWithoutId remindWithoutId) {
		db.insertInto(pr)
				.columns(pr.REMIND_NAME,
						pr.START_DAY_OF_MONTH,
						pr.END_DAY_OF_MONTH,
						pr.HOUR_OF_DAY_TO_FIRE)
				.values(remindWithoutId.name(),
						remindWithoutId.startDayOfMonth(),
						remindWithoutId.endDayOfMonth(),
						remindWithoutId.fireHour())
				.onConflictDoNothing()
				.execute();
	}

	public void addRemind(Remind remind) {
		db.insertInto(pr)
				.columns(pr.ID,
						pr.REMIND_NAME,
						pr.START_DAY_OF_MONTH,
						pr.END_DAY_OF_MONTH,
						pr.HOUR_OF_DAY_TO_FIRE)
				.values(remind.getId(),
						remind.getName(),
						remind.getStartDayOfMonth(),
						remind.getEndDayOfMonth(),
						remind.getHourOfDayToFire())
				.onConflict(pr.ID)
				.doUpdate()
				.set(pr.REMIND_NAME, remind.getName())
				.set(pr.START_DAY_OF_MONTH, remind.getStartDayOfMonth())
				.set(pr.END_DAY_OF_MONTH, remind.getEndDayOfMonth())
				.set(pr.HOUR_OF_DAY_TO_FIRE, remind.getHourOfDayToFire())
				.where(pr.ID.eq(remind.getId()))
				.execute();
	}

	public Set<Remind> getReminds() {
		return db.select(pr.ID,
						pr.REMIND_NAME,
						pr.START_DAY_OF_MONTH,
						pr.END_DAY_OF_MONTH,
						pr.HOUR_OF_DAY_TO_FIRE)
				.from(pr)
				.orderBy(pr.START_DAY_OF_MONTH)
				.fetchSet(mapping(Remind::new));

	}

	@Cacheable("reminds")
	public Remind getRemindById(long id) {
		return db.select(
						pr.ID,
						pr.REMIND_NAME,
						pr.START_DAY_OF_MONTH,
						pr.END_DAY_OF_MONTH,
						pr.HOUR_OF_DAY_TO_FIRE)
				.from(pr)
				.where(pr.ID.eq(id))
				.fetchOne(mapping(Remind::new));
	}

	@CacheEvict(value = "reminds")
	public void deleteRemind(long id) {
		db.delete(phor)
				.where(phor.REMIND_ID.eq(id))
				.execute();
		db.delete(pr)
				.where(pr.ID.eq(id))
				.execute();
	}
}
