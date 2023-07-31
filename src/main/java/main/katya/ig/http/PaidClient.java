package main.katya.ig.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.katya.ig.IGResponseReader;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaidClient extends IgClient {
	@Value("${katya.lamadava.token}")
	private String lamadavaToken;
	@Value("${katya.lamadava.api.url}")
	private String apiUrl;
	private final IGResponseReader responseReader;
	private Header tokenHeader;

	@PostConstruct
	private void init() {
		tokenHeader = new BasicHeader("x-access-key", lamadavaToken);
	}

	@Override
	CloseableHttpResponse execute(RequestBuilder requestBuilder) {
		requestBuilder.addHeader(tokenHeader);
		return super.execute(requestBuilder);
	}

	@Override
	public Optional<String> getMediaUrl(URI uri) {
		RequestBuilder request = RequestBuilder.get(apiUrl + "/media/by/url")
				.addParameter("url", uri.toString());
		CloseableHttpResponse response = execute(request);

		if (getStatusCode(response) == 200) {
			String s = textFromResponse(response);
			return Optional.of(responseReader.getUrlFromSingleMedia(s));
		}
		return Optional.empty();
	}
}
