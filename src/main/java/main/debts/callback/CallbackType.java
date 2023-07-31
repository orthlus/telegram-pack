package main.debts.callback;

public enum CallbackType {
	SELECT_EXPENSE(1);
	private final int id;

	CallbackType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
