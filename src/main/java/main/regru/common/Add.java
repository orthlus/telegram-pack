package main.regru.common;

public class Add extends Basic {
	public final String subdomain;
	public final String ipaddr;

	public Add(String username, String password, String domain_name,
			   String subdomain, String ipaddr) {
		super(username, password, domain_name);
		this.subdomain = subdomain;
		this.ipaddr = ipaddr;
	}
}
