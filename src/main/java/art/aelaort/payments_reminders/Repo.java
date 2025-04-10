package art.aelaort.payments_reminders;

import art.aelaort.dto.entity.Remind;
import art.aelaort.dto.entity.RemindToSend;
import art.aelaort.dto.entity.RemindWithoutId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.tables.PaymentsHoldsOnReminders;
import main.tables.PaymentsReminders;
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
import static main.Tables.PAYMENTS_REMINDERS;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.extract;

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

	public List<RemindToSend> getRemindsForNow() {
		LocalDateTime now = LocalDateTime.now();

		Condition startDayEq = pr.START_DAY_OF_MONTH.lessOrEqual(extract(now, DatePart.DAY));
		Condition endDayEq = pr.END_DAY_OF_MONTH.greaterOrEqual(extract(now, DatePart.DAY));
		Condition timeEq = pr.HOUR_OF_DAY_TO_FIRE.eq(extract(now, DatePart.HOUR));
		Condition holdDateEq = phor.HOLD_END_DT.lessOrEqual(now.toLocalDate());

		return db.select(pr.ID, pr.REMIND_NAME)
				.from(pr)
				.leftJoin(phor)
				.on(pr.ID.eq(phor.REMIND_ID))
				.where(startDayEq
						.and(endDayEq)
						.and(timeEq)
						.and(coalesce(holdDateEq, true)))
				.fetch(mapping(RemindToSend::new));
	}
}
