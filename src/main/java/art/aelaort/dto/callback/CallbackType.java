package art.aelaort.dto.callback;

public enum CallbackType {
	HOLD_ON_PAYMENT_SELECT_DAYS(1),
	SUBMIT_PAYMENT(2);
	private final int id;

	CallbackType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
