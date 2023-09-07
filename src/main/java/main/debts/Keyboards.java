package main.debts;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import main.common.telegram.TgKeyboard;
import main.debts.callback.CallbackMapper;
import main.debts.entity.Expense;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Set;

import static main.debts.callback.CallbackType.SELECT_EXPENSE;
import static main.debts.entity.M.of;

@Component
@RequiredArgsConstructor
public class Keyboards implements TgKeyboard {
	private final Repo repo;
	private final CallbackMapper mapper;

	private String str(String s1, String s2) {
		return "%s - %s".formatted(s1, s2);
	}

	private String sign(Set<Expense> marked, Expense e) {
		String green = "✅";
		String red = "❌";
		return marked.contains(e) ? green : red;
	}

	private String json(Expense e) {
		return mapper.dataToJson(of(e, SELECT_EXPENSE));
	}

	public InlineKeyboardMarkup expenses(Set<Expense> marked) {
		Set<Expense> expenses = repo.getExpenses();

		int size = (expenses.size() / 2) + expenses.size() % 2;
		var iterator = Iterables.paddedPartition(expenses, size).iterator();
		List<Expense> l1 = iterator.next();
		var l2 = iterator.next().iterator();

		var keyboard = emptyKeyboard(expenses.size());
		for (Expense e1 : l1) {
			Expense e2 = l2.next();

			keyboard.add(row(
					btn(str(sign(marked, e1), e1.shortString()), json(e1)),
					e2 == null ? btn() : btn(str(sign(marked, e2), e2.shortString()), json(e2))
			));
		}

		return inlineMarkup(keyboard);
	}
}
