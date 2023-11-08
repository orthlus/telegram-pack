package main.main_tech.ruvds.api;

import feign.RequestLine;

public interface RuvdsHttp {
	@RequestLine("GET /servers")
	ServersRaw servers();
}
