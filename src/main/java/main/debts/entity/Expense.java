package main.debts.entity;

import java.time.LocalDate;
import java.util.Objects;

public record Expense(int id, String name, int amount, int day, LocalDate expireDate)
		implements Comparable<Expense> {
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Expense expense)) return false;

		return Objects.equals(name, expense.name);
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	@Override
	public int compareTo(Expense o) {
		return Integer.compare(day, o.day);
	}

	@Override
	public String toString() {
		return "%03d %-6s %-5d at %2d - %s".formatted(id, name, amount, day, expireDate);
	}

	public String shortString() {
		return "%-6s %-5d at %2d".formatted(name, amount, day);
	}
}
