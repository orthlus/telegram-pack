package main.payments_reminders.reminds;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.Main;
import main.payments_reminders.entity.Remind;
import main.payments_reminders.entity.RemindToSend;
import main.payments_reminders.entity.RemindWithoutId;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static main.Tables.PAYMENTS_HOLDS_ON_REMINDERS;
import static main.tables.PaymentsReminders.PAYMENTS_REMINDERS;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.extract;

@Slf4j
@Component
@RequiredArgsConstructor
public class Repo {
	private final DSLContext db;

	public void addHoldOnRemind(Remind remind, LocalDate holdEndDate) {
		db.insertInto(PAYMENTS_HOLDS_ON_REMINDERS)
				.columns(PAYMENTS_HOLDS_ON_REMINDERS.REMIND_ID, PAYMENTS_HOLDS_ON_REMINDERS.HOLD_END_DT)
				.values(remind.getId(), holdEndDate)
				.onConflict(PAYMENTS_HOLDS_ON_REMINDERS.REMIND_ID)
				.doUpdate()
				.set(PAYMENTS_HOLDS_ON_REMINDERS.HOLD_END_DT, holdEndDate)
				.where(PAYMENTS_HOLDS_ON_REMINDERS.REMIND_ID.eq(remind.getId()))
				.execute();
	}

	public void addRemind(RemindWithoutId remindWithoutId) {
		db.insertInto(PAYMENTS_REMINDERS)
				.columns(PAYMENTS_REMINDERS.REMIND_NAME,
						PAYMENTS_REMINDERS.START_DAY_OF_MONTH,
						PAYMENTS_REMINDERS.END_DAY_OF_MONTH,
						PAYMENTS_REMINDERS.HOUR_OF_DAY_TO_FIRE)
				.values(remindWithoutId.name(),
						remindWithoutId.startDayOfMonth(),
						remindWithoutId.endDayOfMonth(),
						remindWithoutId.fireHour())
				.onConflictDoNothing()
				.execute();
	}

	public void addRemind(Remind remind) {
		db.insertInto(PAYMENTS_REMINDERS)
				.columns(PAYMENTS_REMINDERS.ID,
						PAYMENTS_REMINDERS.REMIND_NAME,
						PAYMENTS_REMINDERS.START_DAY_OF_MONTH,
						PAYMENTS_REMINDERS.END_DAY_OF_MONTH,
						PAYMENTS_REMINDERS.HOUR_OF_DAY_TO_FIRE)
				.values(remind.getId(),
						remind.getName(),
						remind.getStartDayOfMonth(),
						remind.getEndDayOfMonth(),
						remind.getHourOfDayToFire())
				.onConflict(PAYMENTS_REMINDERS.ID)
				.doUpdate()
				.set(PAYMENTS_REMINDERS.REMIND_NAME, remind.getName())
				.set(PAYMENTS_REMINDERS.START_DAY_OF_MONTH, remind.getStartDayOfMonth())
				.set(PAYMENTS_REMINDERS.END_DAY_OF_MONTH, remind.getEndDayOfMonth())
				.set(PAYMENTS_REMINDERS.HOUR_OF_DAY_TO_FIRE, remind.getHourOfDayToFire())
				.where(PAYMENTS_REMINDERS.ID.eq(remind.getId()))
				.execute();
	}

	public Set<Remind> getReminds() {
		return db.select(PAYMENTS_REMINDERS.ID,
						PAYMENTS_REMINDERS.REMIND_NAME,
						PAYMENTS_REMINDERS.START_DAY_OF_MONTH,
						PAYMENTS_REMINDERS.END_DAY_OF_MONTH,
						PAYMENTS_REMINDERS.HOUR_OF_DAY_TO_FIRE)
				.from(PAYMENTS_REMINDERS)
				.orderBy(PAYMENTS_REMINDERS.START_DAY_OF_MONTH)
				.fetchSet(mapping(Remind::new));

	}

	public List<RemindToSend> getRemindsForNow() {
		LocalDateTime now = LocalDateTime.now(Main.zone);

		Condition startDayEq = PAYMENTS_REMINDERS.START_DAY_OF_MONTH.lessOrEqual(extract(now, DatePart.DAY));
		Condition endDayEq = PAYMENTS_REMINDERS.END_DAY_OF_MONTH.greaterOrEqual(extract(now, DatePart.DAY));
		Condition timeEq = PAYMENTS_REMINDERS.HOUR_OF_DAY_TO_FIRE.eq(extract(now, DatePart.HOUR));
		Condition holdDateEq = PAYMENTS_HOLDS_ON_REMINDERS.HOLD_END_DT.lessOrEqual(now.toLocalDate());

		return db.select(PAYMENTS_REMINDERS.ID, PAYMENTS_REMINDERS.REMIND_NAME)
				.from(PAYMENTS_REMINDERS)
				.leftJoin(PAYMENTS_HOLDS_ON_REMINDERS)
				.on(PAYMENTS_REMINDERS.ID.eq(PAYMENTS_HOLDS_ON_REMINDERS.REMIND_ID))
				.where(startDayEq
						.and(endDayEq)
						.and(timeEq)
						.and(coalesce(holdDateEq, true)))
				.fetch(mapping(RemindToSend::new));
	}

	@Cacheable("reminds")
	public Remind getRemindById(long id) {
		return db.select(
						PAYMENTS_REMINDERS.ID,
						PAYMENTS_REMINDERS.REMIND_NAME,
						PAYMENTS_REMINDERS.START_DAY_OF_MONTH,
						PAYMENTS_REMINDERS.END_DAY_OF_MONTH,
						PAYMENTS_REMINDERS.HOUR_OF_DAY_TO_FIRE)
				.from(PAYMENTS_REMINDERS)
				.where(PAYMENTS_REMINDERS.ID.eq(id))
				.fetchOne(mapping(Remind::new));
	}

	@CacheEvict(value = "reminds")
	public void deleteRemind(long id) {
		db.delete(PAYMENTS_HOLDS_ON_REMINDERS)
				.where(PAYMENTS_HOLDS_ON_REMINDERS.REMIND_ID.eq(id))
				.execute();
		db.delete(PAYMENTS_REMINDERS)
				.where(PAYMENTS_REMINDERS.ID.eq(id))
				.execute();
	}
}
