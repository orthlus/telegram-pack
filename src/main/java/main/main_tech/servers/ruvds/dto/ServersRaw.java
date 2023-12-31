package main.main_tech.servers.ruvds.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Set;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServersRaw {
	@JsonProperty("servers")
	private Set<ServerRaw> serverRaws;
}
