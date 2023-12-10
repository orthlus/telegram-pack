package main.regru;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.regru.common.RR;
import main.regru.common.dto.AddAndDeleteDomainResponse;
import main.regru.common.dto.DomainsList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.List;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegRuClient {
	@Value("${regru.api.url}")
	private String baseUrl;
	@Value("${regru.account.user}")
	private String login;
	@Value("${regru.account.password}")
	private String password;

	private WebClient client;

	@PostConstruct
	private void init() {
		HttpClient httpClient = HttpClient.create()
				.option(CONNECT_TIMEOUT_MILLIS, ((int) MINUTES.toMillis(2)));
		client = WebClient.builder()
				.baseUrl(baseUrl)
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.defaultHeaders(h -> h.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
				.exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> {
					ObjectMapper mapper = new ObjectMapper();
					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					MimeType mimeType = MimeTypeUtils.parseMimeType(MediaType.TEXT_PLAIN_VALUE);
					Jackson2JsonDecoder codec = new Jackson2JsonDecoder(mapper, mimeType);
					configurer.customCodecs().register(codec);
				}).build())
				.build();
	}

	private BodyInserters.FormInserter<String> basicFormData(String domainName) {
		return fromFormData("username", login)
				.with("password", password)
				.with("domain_name", domainName);
	}

	public List<RR> getSubdomainsList(String domainName) {
		return client.post()
				.uri("/zone/get_resource_records")
				.body(basicFormData(domainName))
				.retrieve()
				.bodyToMono(DomainsList.class)
				.map(this::extractList)
				.block();
	}

	public boolean deleteSubdomain(RR record, String domainName) {
		return client.post()
				.uri("/zone/remove_record")
				.body(basicFormData(domainName)
						.with("record_type", "A")
						.with("subdomain", record.domain())
						.with("content", record.ip()))
				.retrieve()
				.bodyToMono(AddAndDeleteDomainResponse.class)
				.onErrorReturn(getEmptyAddAndDeleteDomainResponse())
				.block()
				.isResultSuccess();
	}

	public boolean addSubdomain(RR record, String domainName) {
		return client.post()
				.uri("/zone/add_alias")
				.body(basicFormData(domainName)
						.with("subdomain", record.domain())
						.with("ipaddr", record.ip()))
				.retrieve()
				.bodyToMono(AddAndDeleteDomainResponse.class)
				.onErrorReturn(getEmptyAddAndDeleteDomainResponse())
				.block()
				.isResultSuccess();
	}

	private AddAndDeleteDomainResponse getEmptyAddAndDeleteDomainResponse() {
		AddAndDeleteDomainResponse o = new AddAndDeleteDomainResponse();
		o.result = "";
		return o;
	}

	private List<RR> extractList(DomainsList domainsList) {
		if (domainsList.isResultSuccess()) {
			return domainsList.getList()
					.stream()
					.filter(rrDto -> rrDto.rectype.equals("A"))
					.map(rrDto -> new RR(rrDto.content, rrDto.subname))
					.toList();
		}

		return List.of();
	}
}
