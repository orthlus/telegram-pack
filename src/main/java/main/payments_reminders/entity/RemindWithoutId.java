package main.payments_reminders.entity;

public record RemindWithoutId(
		String name,
		int startDayOfMonth,
		int endDayOfMonth,
		int fireHour
) {
	@Override
	public String toString() {
		return """
				%s
				с %d по %d каждый месяц
				в %d:00""".formatted(name, startDayOfMonth, endDayOfMonth, fireHour);
	}
}
