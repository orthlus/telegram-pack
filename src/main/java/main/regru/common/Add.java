package main.regru.common;

import lombok.Getter;

@Getter
public class Add extends Basic {
	private String subdomain;
	private String ipaddr;

	public Add(String username, String password, String domain_name,
			   String subdomain, String ipaddr) {
		super(username, password, domain_name);
		this.subdomain = subdomain;
		this.ipaddr = ipaddr;
	}
}
