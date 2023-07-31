package main.debts.callback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.debts.DebtsService;
import main.debts.entity.Expense;
import main.debts.entity.M;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackMapper {
	private final DebtsService service;
	private ObjectMapper mapper = new ObjectMapper();
	private Map<Integer, CallbackType> typesById = new HashMap<>();

	{
		for (CallbackType type : CallbackType.values()) typesById.put(type.getId(), type);
	}

	public Optional<Expense> getExpenseFromQuery(CallbackQuery query) {
		if (getTypeFromQuery(query).isEmpty()) return Optional.empty();

		Optional<Integer> expenseIdOp = getDataFromQuery(query, M.ExpenseCallback.class)
				.map(M.ExpenseCallback::expenseId);
		return service.getExpenseById(expenseIdOp.orElseThrow());
	}

	public Optional<CallbackType> getTypeFromQuery(CallbackQuery callbackQuery) {
		try {
			M.SomeCallback someCallback = mapper.readValue(callbackQuery.getData(), M.SomeCallback.class);
			return Optional.ofNullable(typesById.get(someCallback.typeId()));
		} catch (JsonProcessingException e) {
			log.error("Error parsing callback type from query: {}", callbackQuery.getData(), e);
			return Optional.empty();
		}
	}

	public String dataToJson(CallbackData data) {
		try {
			return mapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			log.error("Error serialize callback data {}", data, e);
			throw new RuntimeException(e);
		}
	}

	private <T extends CallbackData> Optional<T> getDataFromQuery(CallbackQuery query, Class<T> clazz) {
		if (getTypeFromQuery(query).isPresent()) {
			return fromJson(query.getData(), clazz);
		}

		log.error("Error getting type from query {}", query);
		return Optional.empty();
	}

	private <T extends CallbackData> Optional<T> fromJson(String jsonStr, Class<T> dtoType) {
		try {
			return Optional.of(mapper.readValue(jsonStr, dtoType));
		} catch (JsonProcessingException e) {
			log.error("Error parsing {} callback from {}", dtoType, jsonStr, e);
			return Optional.empty();
		}
	}
}
