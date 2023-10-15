package main.common.telegram;

public interface UsingPrivateApi {
	default boolean isPrivateApi() {
		return false;
	}
}
