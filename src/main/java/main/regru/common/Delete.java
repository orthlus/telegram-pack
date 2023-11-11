package main.regru.common;

public class Delete extends Basic {
	public final String subdomain;
	public final String content;
	public final String record_type;

	public Delete(String username, String password, String domain_name,
				  String subdomain, String content, String record_type) {
		super(username, password, domain_name);
		this.subdomain = subdomain;
		this.content = content;
		this.record_type = record_type;
	}
}
