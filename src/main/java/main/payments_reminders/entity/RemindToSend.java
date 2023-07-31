package main.payments_reminders.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemindToSend {
	private long id;
	private String name;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "RemindToSend{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
