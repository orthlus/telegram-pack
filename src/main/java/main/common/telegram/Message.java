package main.common.telegram;

public record Message(long chatId, int messageId) {
	public static Message empty() {
		return new Message(0, 0);
	}

	public boolean notEmpty() {
		return chatId != 0 && messageId != 0;
	}

	public static Message of(org.telegram.telegrambots.meta.api.objects.Message message) {
		return new Message(message.getChatId(), message.getMessageId());
	}
}
