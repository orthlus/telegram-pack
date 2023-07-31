package main.regru.common;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static main.regru.common.ChatStates.NOTHING_WAIT;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChatState {
	private final Map<String, String> values = new ConcurrentHashMap<>();
	private final AtomicReference<ChatStates> currentState = new AtomicReference<>(NOTHING_WAIT);

	public void setState(ChatStates state) {
		currentState.set(state);
	}

	public ChatStates getCurrentState() {
		return currentState.get();
	}

	public void addValue(ChatStates key, String value) {
		addValue(key.toString(), value);
	}

	public void addValue(String key, String value) {
		values.put(key, value);
	}

	public String getAndDeleteValue(ChatStates key) {
		return getAndDeleteValue(key.toString());
	}

	public String getAndDeleteValue(String key) {
		return values.remove(key);
	}
}
