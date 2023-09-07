package main.main_tech.ruvds.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import main.common.HttpClient;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

import static org.mapstruct.factory.Mappers.getMapper;

@Slf4j
@Component
public class RuvdsApi extends HttpClient {
	@Value("${main_tech.ruvds.api.url}")
	private String url;
	@Value("${main_tech.ruvds.api.token}")
	private String token;
	private final ObjectMapper mapper = new ObjectMapper();

	public Set<Server> getServers() {
		return getMapper(ServerMapper.class).map(getServers0());
	}

	private ServersRaw getServers0() {
		Request request = new Request.Builder().get()
				.url(url + "/servers")
				.addHeader("Authorization", "Bearer " + token)
				.build();
		Call call = baseHttpClient.newCall(request);
		try {
			Response response = call.execute();
			String bodyStr = readBody(response);
			if (response.isSuccessful()) {
				return mapper.readValue(bodyStr, ServersRaw.class);
			}
			log.error("http error - ruvds getServers, response code - {}, body - {}", response.code(), bodyStr);
		} catch (IOException e) {
			log.error("http error - ruvds getServers", e);
		}
		throw new RuntimeException("error ruvds getServers");
	}
}
