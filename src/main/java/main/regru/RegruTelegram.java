package main.regru;

import main.common.telegram.DefaultWebhookBot;
import main.regru.common.ChatState;
import main.regru.common.RR;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;

import static com.google.common.net.InetAddresses.isInetAddress;
import static main.regru.common.ChatStates.*;

abstract class RegruTelegram implements DefaultWebhookBot {
	private final RegRuService regRuService;
	private final ChatState chatState;

	public RegruTelegram(RegRuService regRuService, ChatState chatState) {
		this.regRuService = regRuService;
		this.chatState = chatState;
	}

	abstract String getDomainName();

	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (!update.hasMessage()) return null;
		if (!update.getMessage().hasText()) return null;

		if (update.getMessage().getText().startsWith("/")) {
			return handleCommand(update);
		} else {
			return handleText(update);
		}
	}

	private BotApiMethod<?> handleText(Update update) {
		SendMessage result = null;
		String text = update.getMessage().getText();
		switch (chatState.currentState.get()) {
			case NOTHING_WAIT -> result = send("работает");
			case WAIT_IP_TO_ADD -> {
				if (isValidIp(text)) {
					chatState.currentState.set(WAIT_DOMAIN_NAME_TO_ADD);
					chatState.addValue(WAIT_IP_TO_ADD, text);
					result = send("domain:");
				} else {
					result = send("invalid, try again\nip:");
				}
			}
			case WAIT_DOMAIN_NAME_TO_ADD -> {
				if (isValidDomain(text)) {
					RR rr = new RR(chatState.getAndDeleteValue(WAIT_IP_TO_ADD), text);
					boolean add = regRuService.addSubdomain(rr, getDomainName());
					if (add) {
						chatState.currentState.set(NOTHING_WAIT);
						result = send("::ok::\n/list");
					} else {
						result = send("error :( need logs");
					}
				} else {
					result = send("invalid, try again\ndomain:");
				}
			}
			case WAIT_IP_TO_DELETE -> {
				if (isValidIp(text)) {
					chatState.currentState.set(WAIT_DOMAIN_NAME_TO_DELETE);
					chatState.addValue(WAIT_IP_TO_DELETE, text);
					result = send("domain:");
				} else {
					result = send("invalid, try again\nip:");
				}
			}
			case WAIT_DOMAIN_NAME_TO_DELETE -> {
				if (isValidDomain(text)) {
					RR rr = new RR(chatState.getAndDeleteValue(WAIT_IP_TO_DELETE), text);
					boolean delete = regRuService.deleteSubdomain(rr, getDomainName());
					if (delete) {
						chatState.currentState.set(NOTHING_WAIT);
						result = send("::ok::\n/list");
					} else {
						result = send("error :( need logs");
					}
				} else {
					result = send("invalid, try again\ndomain:");
				}
			}
		}
		return result;
	}

	private BotApiMethod<?> handleCommand(Update update) {
		String text = update.getMessage().getText();
		switch (text) {
			case "/start" -> {
				return send("Тут можно управлять поддоменами домена " + getDomainName());
			}
			case "/list" -> {
				return sendList();
			}
			case "/add" -> {
				chatState.currentState.set(WAIT_IP_TO_ADD);
				return send("ip:");
			}
			case "/delete" -> {
				chatState.currentState.set(WAIT_IP_TO_DELETE);
				return send("ip:");
			}
			case "/cancel" -> {
				chatState.currentState.set(NOTHING_WAIT);
				return send("canceled");
			}
		}
		return null;
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
