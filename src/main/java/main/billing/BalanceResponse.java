package main.billing;

import static org.apache.commons.lang3.StringUtils.capitalize;

public interface BalanceResponse {
	org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BalanceResponse.class);

	String balanceString();

	String name();

	default String text() {
		try {
			return "*%s:*\n%s".formatted(name(), balanceString());
		} catch (Exception e) {
			return error(name(), e);
		}
	}

	default String error(String name, Exception e) {
		log.error("billing {} error", capitalize(name), e);
		return "*%s:* error".formatted(name);
	}
}
