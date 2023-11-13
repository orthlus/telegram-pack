package main.regru.common;

import lombok.Getter;

@Getter
public class Basic {
	private String username;
	private String password;
	private String domain_name;

	public Basic(String username, String password, String domain_name) {
		this.username = username;
		this.password = password;
		this.domain_name = domain_name;
	}
}
