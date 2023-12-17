package main.domains.common;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static main.domains.common.ChatStates.NOTHING_WAIT;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChatState {
	private final Map<String, String> values = new ConcurrentHashMap<>();
	public final AtomicReference<ChatStates> currentState = new AtomicReference<>(NOTHING_WAIT);

	public void addValue(ChatStates key, String value) {
		values.put(key.toString(), value);
	}

	public String getAndDeleteValue(ChatStates key) {
		return values.remove(key.toString());
	}
}
