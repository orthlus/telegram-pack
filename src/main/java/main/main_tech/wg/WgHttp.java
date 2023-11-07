package main.main_tech.wg;

import feign.RequestLine;

public interface WgHttp {
	@RequestLine("GET /raw-stat")
	String raw();

	@RequestLine("GET /pretty-stat")
	String pretty();

	@RequestLine("GET /users")
	String users();
}
