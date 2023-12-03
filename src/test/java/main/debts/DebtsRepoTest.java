package main.debts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;


@SpringBootTest(useMainMethod = ALWAYS, webEnvironment = NONE)
@ActiveProfiles(profiles = "test")
class DebtsRepoTest {
	@Autowired
	private DebtsService debtsService;

	@Test
	public void getExpenses() {
		System.out.println(debtsService.getExpensesText());
	}
}