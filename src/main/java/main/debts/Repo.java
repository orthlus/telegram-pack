package main.debts;

import lombok.RequiredArgsConstructor;
import main.debts.entity.Expense;
import main.debts.entity.Income;
import main.tables.DebtsExpenses;
import main.tables.DebtsIncome;
import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

import static main.Main.zone;
import static main.Tables.DEBTS_EXPENSES;
import static main.Tables.DEBTS_INCOME;
import static org.jooq.Records.mapping;

@Component
@RequiredArgsConstructor
public class Repo {
	private final DSLContext db;
	private final DebtsIncome di = DEBTS_INCOME;
	private final DebtsExpenses de = DEBTS_EXPENSES;

	@CacheEvict(value = "incomes", allEntries = true)
	public void deleteIncome(int id) {
		db.delete(di).where(di.ID.eq(id)).execute();
	}

	@CacheEvict(value = "incomes", allEntries = true)
	public void addIncome(int amount, int day) {
		db.insertInto(di)
				.columns(di.AMOUNT, di.DAY_OF_MONTH)
				.values(amount, day)
				.execute();
	}

	@Cacheable("incomes")
	public Set<Income> getIncomes() {
		return db.select(di.ID, di.AMOUNT, di.DAY_OF_MONTH)
				.from(di)
				.orderBy(di.DAY_OF_MONTH)
				.fetchSet(mapping(Income::new));
	}

	@CacheEvict(value = "expenses", allEntries = true)
	public void deleteExpense(int id) {
		db.delete(de).where(de.ID.eq(id)).execute();
	}

	@CacheEvict(value = "expenses", allEntries = true)
	public void addExpense(String name, int amount, int day, LocalDate expireDate) {
		db.insertInto(de)
				.columns(de.EXP_NAME,
						de.AMOUNT,
						de.DAY_OF_MONTH,
						de.EXPIRE_DATE)
				.values(name, amount, day, expireDate)
				.execute();
	}

	@Cacheable(value = "expenses")
	public Set<Expense> getExpenses() {
		return getExpenses(LocalDate.now(zone));
	}

	public Set<Expense> getExpenses(LocalDate date) {
		return db.select(de.ID,
						de.EXP_NAME,
						de.AMOUNT,
						de.DAY_OF_MONTH,
						de.EXPIRE_DATE)
				.from(de)
				.where(de.EXPIRE_DATE.greaterOrEqual(date))
				.orderBy(de.DAY_OF_MONTH, de.EXP_NAME)
				.fetchSet(mapping(Expense::new));
	}
}
