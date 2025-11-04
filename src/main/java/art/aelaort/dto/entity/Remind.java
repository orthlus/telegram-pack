package art.aelaort.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Remind {
	private long id;
	private String name;
	private int startDayOfMonth;
	private int endDayOfMonth;
	private int hourOfDayToFire;

	@Override
	public String toString() {
		String sings = "";
		if (name.contains("счетчики")) sings += "🔢";
		if (name.contains("передать")) sings += "🔢";
		if (name.contains("оплатить")) sings += "💰";
		String oName = name
				.replace("счетчики", "")
				.replace("передать", "")
				.replace("оплатить", "")
				.replaceAll(" +", " ")
				.trim();
		return "%02d %-18s %2d - %2d %2d %s".formatted(id, oName, startDayOfMonth, endDayOfMonth, hourOfDayToFire, sings);
	}
}
