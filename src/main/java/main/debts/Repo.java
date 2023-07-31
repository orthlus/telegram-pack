package main.debts;

import lombok.RequiredArgsConstructor;
import main.debts.entity.Expense;
import main.debts.entity.Income;
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

	@CacheEvict(value = "incomes", allEntries = true)
	public void deleteIncome(int id) {
		db.delete(DEBTS_INCOME).where(DEBTS_INCOME.ID.eq(id)).execute();
	}

	@CacheEvict(value = "incomes", allEntries = true)
	public void addIncome(int amount, int day) {
		db.insertInto(DEBTS_INCOME)
				.columns(DEBTS_INCOME.AMOUNT, DEBTS_INCOME.DAY_OF_MONTH)
				.values(amount, day)
				.execute();
	}

	@Cacheable("incomes")
	public Set<Income> getIncomes() {
		return db.select(DEBTS_INCOME.ID, DEBTS_INCOME.AMOUNT, DEBTS_INCOME.DAY_OF_MONTH)
				.from(DEBTS_INCOME)
				.orderBy(DEBTS_INCOME.DAY_OF_MONTH)
				.fetchSet(mapping(Income::new));
	}

	@CacheEvict(value = "expenses", allEntries = true)
	public void deleteExpense(int id) {
		db.delete(DEBTS_EXPENSES).where(DEBTS_EXPENSES.ID.eq(id)).execute();
	}

	@CacheEvict(value = "expenses", allEntries = true)
	public void addExpense(String name, int amount, int day, LocalDate expireDate) {
		db.insertInto(DEBTS_EXPENSES)
				.columns(DEBTS_EXPENSES.EXP_NAME,
						DEBTS_EXPENSES.AMOUNT,
						DEBTS_EXPENSES.DAY_OF_MONTH,
						DEBTS_EXPENSES.EXPIRE_DATE)
				.values(name, amount, day, expireDate)
				.execute();
	}

	@Cacheable(value = "expenses")
	public Set<Expense> getExpenses() {
		return db.select(DEBTS_EXPENSES.ID,
						DEBTS_EXPENSES.EXP_NAME,
						DEBTS_EXPENSES.AMOUNT,
						DEBTS_EXPENSES.DAY_OF_MONTH,
						DEBTS_EXPENSES.EXPIRE_DATE)
				.from(DEBTS_EXPENSES)
				.where(DEBTS_EXPENSES.EXPIRE_DATE.greaterOrEqual(LocalDate.now(zone)))
				.orderBy(DEBTS_EXPENSES.DAY_OF_MONTH, DEBTS_EXPENSES.EXP_NAME)
				.fetchSet(mapping(Expense::new));
	}
}
