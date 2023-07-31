package main.debts.entity;

public record Income(int id, int amount, int day) {
	@Override
	public String toString() {
		return "id %d amount %d day %d".formatted(id, amount, day);
	}
}
