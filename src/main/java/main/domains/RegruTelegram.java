package main.domains;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.common.telegram.Command;
import main.common.telegram.DefaultWebhookBot;
import main.domains.common.ChatState;
import main.domains.common.RR;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.Map;

import static com.google.common.net.InetAddresses.isInetAddress;
import static main.domains.common.ChatStates.*;

@RequiredArgsConstructor
abstract class RegruTelegram implements DefaultWebhookBot {
	@AllArgsConstructor
	@Getter
	private enum Commands implements Command {
		START("/start"),
		LIST("/list"),
		ADD("/add"),
		DELETE("/delete"),
		CANCEL("/cancel");
		final String command;
	}

	private final RegRuService regRuService;
	private final ChatState chatState;
	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);

	abstract String getDomainName();

	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (!update.hasMessage()) return null;
		if (!update.getMessage().hasText()) return null;

		String messageText = update.getMessage().getText();

		if (update.getMessage().getText().startsWith("/")) {
			return handleCommand(messageText);
		} else {
			return handleText(messageText);
		}
	}

	private BotApiMethod<?> handleText(String messageText) {
		return switch (chatState.currentState.get()) {
			case NOTHING_WAIT -> send("работает");
			case WAIT_IP_TO_ADD -> {
				if (isValidIp(messageText)) {
					chatState.currentState.set(WAIT_DOMAIN_NAME_TO_ADD);
					chatState.addValue(WAIT_IP_TO_ADD, messageText);
					yield send("domain:");
				} else {
					yield send("invalid, try again\nip:");
				}
			}
			case WAIT_DOMAIN_NAME_TO_ADD -> {
				if (isValidDomain(messageText)) {
					RR rr = new RR(chatState.getAndDeleteValue(WAIT_IP_TO_ADD), messageText);
					boolean add = regRuService.addSubdomain(rr, getDomainName());
					if (add) {
						chatState.currentState.set(NOTHING_WAIT);
						yield send("::ok::\n/list");
					} else {
						yield send("error :( need logs");
					}
				} else {
					yield send("invalid, try again\ndomain:");
				}
			}
			case WAIT_IP_TO_DELETE -> {
				if (isValidIp(messageText)) {
					chatState.currentState.set(WAIT_DOMAIN_NAME_TO_DELETE);
					chatState.addValue(WAIT_IP_TO_DELETE, messageText);
					yield send("domain:");
				} else {
					yield send("invalid, try again\nip:");
				}
			}
			case WAIT_DOMAIN_NAME_TO_DELETE -> {
				if (isValidDomain(messageText)) {
					RR rr = new RR(chatState.getAndDeleteValue(WAIT_IP_TO_DELETE), messageText);
					boolean delete = regRuService.deleteSubdomain(rr, getDomainName());
					if (delete) {
						chatState.currentState.set(NOTHING_WAIT);
						yield send("::ok::\n/list");
					} else {
						yield send("error :( need logs");
					}
				} else {
					yield send("invalid, try again\ndomain:");
				}
			}
		};
	}

	private BotApiMethod<?> handleCommand(String messageText) {
		return switch (commandsMap.get(messageText)) {
			case START -> send("Тут можно управлять поддоменами домена " + getDomainName());
			case LIST -> sendList();
			case ADD -> {
				chatState.currentState.set(WAIT_IP_TO_ADD);
				yield send("ip:");
			}
			case DELETE -> {
				chatState.currentState.set(WAIT_IP_TO_DELETE);
				yield send("ip:");
			}
			case CANCEL -> {
				chatState.currentState.set(NOTHING_WAIT);
				yield send("canceled");
			}
		};
	}

	private boolean isValidDomain(String domainStr) {
		return domainStr.replaceAll("[a-zA-Z0-9-@]+", "").isEmpty();
	}

	private boolean isValidIp(String ipStr) {
		return isInetAddress(ipStr);
	}

	private SendMessage sendList() {
		StringBuilder msgContent = new StringBuilder("Список поддоменов и адресов домена %s:\n".formatted(getDomainName()));
		regRuService.getSubdomainsList(getDomainName())
				.stream()
				.sorted(Comparator.comparing(RR::ip))
				.forEach(rr -> {
					msgContent.append(rr.ip());
					msgContent.append(" - ");
					msgContent.append(rr.domain());
					msgContent.append("\n");
				});
		msgContent.deleteCharAt(msgContent.length() - 1);
		return send(msgContent.toString());
	}
}
