package art.aelaort.debts.entity;

import java.time.LocalDate;

public record ExpenseDto(String name, int amount, int day, LocalDate expire) {
}
