package main.regru;

import com.google.common.net.InetAddresses;
import main.common.telegram.BotConfig;
import main.common.telegram.CustomSpringWebhookBot;
import main.regru.common.ChatState;
import main.regru.common.RR;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;

import static main.regru.common.ChatStates.*;

public class RegruTelegram extends CustomSpringWebhookBot {
	private final RegRuClient regRuClient;
	private final ChatState chatState;
	private final String domainName;

	public RegruTelegram(BotConfig botConfig,
						 RegRuClient regRuClient,
						 ChatState chatState,
						 String domainName) {
		super(botConfig);
		this.regRuClient = regRuClient;
		this.chatState = chatState;
		this.domainName = domainName;
	}

	public void onWebhookUpdate(Update update) {
		if (!update.hasMessage()) return;
		if (!update.getMessage().hasText()) return;

		if (update.getMessage().getText().startsWith("/")) {
			handleCommand(update);
		} else {
			handleText(update);
		}
	}

	private void handleText(Update update) {
		String text = update.getMessage().getText();
		switch (chatState.getCurrentState()) {
			case NOTHING_WAIT -> send("работает");
			case WAIT_IP_TO_ADD -> {
				if (isValidIp(text)) {
					chatState.setState(WAIT_DOMAIN_NAME_TO_ADD);
					chatState.addValue(WAIT_IP_TO_ADD, text);
					send("domain:");
				} else {
					send("invalid, try again\nip:");
				}
			}
			case WAIT_DOMAIN_NAME_TO_ADD -> {
				if (isValidDomain(text)) {
					RR rr = new RR(chatState.getAndDeleteValue(WAIT_IP_TO_ADD), text);
					boolean add = regRuClient.addSubdomain(rr, domainName);
					if (add) {
						chatState.setState(NOTHING_WAIT);
						send("::ok::\n/list");
						sendList();
					} else send("error :( need logs");
				} else {
					send("invalid, try again\ndomain:");
				}
			}
			case WAIT_IP_TO_DELETE -> {
				if (isValidIp(text)) {
					chatState.setState(WAIT_DOMAIN_NAME_TO_DELETE);
					chatState.addValue(WAIT_IP_TO_DELETE, text);
					send("domain:");
				} else {
					send("invalid, try again\nip:");
				}
			}
			case WAIT_DOMAIN_NAME_TO_DELETE -> {
				if (isValidDomain(text)) {
					RR rr = new RR(chatState.getAndDeleteValue(WAIT_IP_TO_DELETE), text);
					boolean delete = regRuClient.deleteSubdomain(rr, domainName);
					if (delete) {
						chatState.setState(NOTHING_WAIT);
						send("::ok::\n/list");
						sendList();
					} else send("error :( need logs");
				} else {
					send("invalid, try again\ndomain:");
				}
			}
		}
	}

	private void handleCommand(Update update) {
		String text = update.getMessage().getText();
		switch (text) {
			case "/start" -> send("Тут можно управлять поддоменами домена " + domainName);
			case "/list" -> sendList();
			case "/add" -> addSubDomain();
			case "/delete" -> deleteSubDomain();
			case "/cancel" -> {
				send("canceled");
				chatState.setState(NOTHING_WAIT);
			}
		}
	}

	private void deleteSubDomain() {
		send("ip:");
		chatState.setState(WAIT_IP_TO_DELETE);
	}

	private void addSubDomain() {
		send("ip:");
		chatState.setState(WAIT_IP_TO_ADD);
	}

	private boolean isValidDomain(String domainStr) {
		return domainStr.replaceAll("[a-zA-Z0-9-@]+", "").length() == 0;
	}

	private boolean isValidIp(String ipStr) {
		return InetAddresses.isInetAddress(ipStr);
	}

	private void sendList() {
		StringBuilder msgContent = new StringBuilder("Список поддоменов и адресов домена %s:\n".formatted(domainName));
		regRuClient.getSubdomainsList(domainName)
				.stream()
				.sorted(Comparator.comparing(RR::ip))
				.forEach(rr -> {
					msgContent.append(rr.ip());
					msgContent.append(" - ");
					msgContent.append(rr.domain());
					msgContent.append("\n");
				});
		msgContent.deleteCharAt(msgContent.length() - 1);
		send(msgContent.toString());
	}
}
