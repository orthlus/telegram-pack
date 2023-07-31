package main.debts.entity;

import java.time.LocalDate;

public record Expense(int id, String name, int amount, int day, LocalDate expireDate) {
	@Override
	public String toString() {
		return "%03d %-6s %-5d at %2d - %s".formatted(id, name, amount, day, expireDate);
	}

	public String shortString() {
		return "%-6s %-5d at %2d".formatted(name, amount, day);
	}
}
