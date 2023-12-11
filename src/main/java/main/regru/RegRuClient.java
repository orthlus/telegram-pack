package main.regru;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.regru.common.RR;
import main.regru.common.dto.AddAndDeleteDomainResponse;
import main.regru.common.dto.DomainsList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegRuClient {
	@Value("${regru.account.user}")
	private String login;
	@Value("${regru.account.password}")
	private String password;

	@Qualifier("regruWebClient")
	private final WebClient client;

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
