package main.regru.common;

import lombok.Getter;

@Getter
public class Delete extends Basic {
	private String subdomain;
	private String content;
	private String record_type;

	public Delete(String username, String password, String domain_name,
				  String subdomain, String content, String record_type) {
		super(username, password, domain_name);
		this.subdomain = subdomain;
		this.content = content;
		this.record_type = record_type;
	}
}
