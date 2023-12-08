package main.main_tech.servers.ruvds;

import feign.RequestLine;
import main.main_tech.servers.ruvds.dto.ServersRaw;

public interface RuvdsHttp {
	@RequestLine("GET /servers")
	ServersRaw servers();
}
