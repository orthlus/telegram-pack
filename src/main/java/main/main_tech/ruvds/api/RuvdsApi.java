package main.main_tech.ruvds.api;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Set;

import static org.mapstruct.factory.Mappers.getMapper;

@Slf4j
@Component
public class RuvdsApi {
	@Value("${main_tech.ruvds.api.url}")
	private String url;
	@Value("${main_tech.ruvds.api.token}")
	private String token;
	private RuvdsHttp client;

	@PostConstruct
	private void init() {
		client = Feign.builder()
				.decoder(new JacksonDecoder())
				.requestInterceptor(template -> template.header("Authorization", "Bearer " + token))
				.target(RuvdsHttp.class, url);
	}

	public Set<RuvdsServer> getServers() {
		return getMapper(ServerMapper.class).map(client.servers());
	}
}
