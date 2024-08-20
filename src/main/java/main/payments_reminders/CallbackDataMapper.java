package main.payments_reminders;

import art.aelaort.telegram.callback.CallbackType;
import art.aelaort.telegram.callback.models.CallbackData;
import art.aelaort.telegram.callback.models.RemindCallback;
import art.aelaort.telegram.callback.models.RemindDaysCallback;
import art.aelaort.telegram.callback.models.SomeCallback;
import art.aelaort.telegram.entity.Remind;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackDataMapper {
	private final Repo repo;
	private final ObjectMapper mapper;
	private Map<Integer, CallbackType> typesById = new HashMap<>();

	{
		for (CallbackType type : CallbackType.values()) typesById.put(type.getId(), type);
	}

	public Optional<Tuple2<Remind, Integer>> getRemindAndDaysFromCallback(CallbackQuery callbackQuery) {
		Optional<RemindDaysCallback> dataOp = getDataFromQuery(callbackQuery, RemindDaysCallback.class);
		return dataOp.map(r -> new Tuple2<>(repo.getRemindById(dataOp.get().remindId()), dataOp.get().days()));
	}

	public Optional<Remind> getRemindFromCallback(CallbackQuery query) {
		if (getTypeFromQuery(query).isEmpty()) return Optional.empty();

		return getDataFromQuery(query, RemindCallback.class)
				.map(remindCallback -> repo.getRemindById(remindCallback.remindId()));
	}

	public Optional<CallbackType> getTypeFromQuery(CallbackQuery callbackQuery) {
		try {
			SomeCallback someCallback = mapper.readValue(callbackQuery.getData(), SomeCallback.class);
			return Optional.of(typesById.get(someCallback.typeId()));
		} catch (JsonProcessingException e) {
			log.error("Error parsing callback type from query: {}", callbackQuery.getData(), e);
			return Optional.empty();
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
