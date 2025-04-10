package art.aelaort.debts;

import lombok.RequiredArgsConstructor;
import art.aelaort.debts.entity.Expense;
import art.aelaort.debts.entity.ExpenseDto;
import art.aelaort.debts.entity.Income;
import art.aelaort.debts.entity.IncomeDto;
import main.tables.DebtsExpenses;
import main.tables.DebtsIncome;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static main.Tables.DEBTS_EXPENSES;
import static main.Tables.DEBTS_INCOME;
import static org.jooq.Records.mapping;

@Component
@RequiredArgsConstructor
public class DebtsRepo {
	private final DSLContext db;
	private final DebtsIncome di = DEBTS_INCOME;
	private final DebtsExpenses de = DEBTS_EXPENSES;

	public void deleteIncome(int id) {
		db.delete(di).where(di.ID.eq(id)).execute();
	}

	public void addIncome(IncomeDto data) {
		db.insertInto(di)
				.columns(di.AMOUNT, di.DAY_OF_MONTH)
				.values(data.amount(), data.day())
				.execute();
	}

	public Set<Income> getIncomes() {
		return db.select(di.ID, di.AMOUNT, di.DAY_OF_MONTH)
				.from(di)
				.orderBy(di.DAY_OF_MONTH)
				.fetchSet(mapping(Income::new));
	}

	public void deleteExpense(int id) {
		db.delete(de).where(de.ID.eq(id)).execute();
	}

	public void addExpense(ExpenseDto data) {
		db.insertInto(de)
				.columns(de.EXP_NAME,
						de.AMOUNT,
						de.DAY_OF_MONTH,
						de.EXPIRE_DATE)
				.values(data.name(), data.amount(), data.day(), data.expire())
				.execute();
	}

	public List<Expense> getExpenses() {
		return getExpenses(LocalDate.now());
	}

	public List<Expense> getExpenses(LocalDate date) {
		return db.select(de.ID,
						de.EXP_NAME,
						de.AMOUNT,
						de.DAY_OF_MONTH,
						de.EXPIRE_DATE)
				.from(de)
				.where(de.EXPIRE_DATE.greaterOrEqual(date))
				.orderBy(de.DAY_OF_MONTH, de.EXP_NAME)
				.fetch(mapping(Expense::new));
	}
}
