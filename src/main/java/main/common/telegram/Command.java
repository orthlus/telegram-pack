package main.common.telegram;

import java.util.HashMap;
import java.util.Map;

public interface Command {
	String getCommand();

	static <T extends Command> Map<String, T> buildMap(Class<T> commandsClass) {
		if (commandsClass.isEnum()) {
			Map<String, T> commandsMap = new HashMap<>();
			for (T command : commandsClass.getEnumConstants())
				commandsMap.put(command.getCommand(), command);
			return commandsMap;

		}
		throw new RuntimeException("passed not enum class");
	}
}
