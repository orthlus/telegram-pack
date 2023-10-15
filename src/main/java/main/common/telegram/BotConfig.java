package main.common.telegram;

public interface BotConfig extends UsingPrivateApi {
	String getToken();
	String getNickname();
}
