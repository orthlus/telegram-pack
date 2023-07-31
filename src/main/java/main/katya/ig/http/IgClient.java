package main.katya.ig.http;

import lombok.extern.slf4j.Slf4j;
import main.katya.exception.InstagramUnauthorizedException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Slf4j
abstract public class IgClient {
	private final CloseableHttpClient httpClient;

	{
		int timeout = 10000;
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeout)
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
		httpClient = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig)
				.build();
	}

	CloseableHttpResponse execute(RequestBuilder requestBuilder) {
		URI uri = requestBuilder.getUri();
		log.info("start request {}", uri);
		try {
			CloseableHttpResponse response = httpClient.execute(requestBuilder.build());
			log.info("request {}, response code {}", uri, getStatusCode(response));
			return response;
		} catch (IOException e) {
			log.error("http error InstagramClient, uri - {}", uri, e);
			throw new RuntimeException(e);
		}
	}

	String textFromResponse(CloseableHttpResponse response) {
		try {
			return EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			log.error("error read response {}", response, e);
			return "";
		}
	}

	int getStatusCode(CloseableHttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == 401) {
			throw new InstagramUnauthorizedException();
		}

		return statusCode;
	}

	abstract public Optional<String> getMediaUrl(URI uri);
}
