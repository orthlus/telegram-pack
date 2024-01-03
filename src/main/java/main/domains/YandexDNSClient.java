package main.domains;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.domains.common.RR;
import main.domains.common.dto.yandex.ListRecordSetsResponse;
import main.domains.common.dto.yandex.UpdateDNSRecordsResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static java.lang.Boolean.TRUE;

@Slf4j
@RequiredArgsConstructor
public abstract class YandexDNSClient implements DNSClientOperations {
	private final WebClient client;
	private final YandexApiTokenService tokenService;
	private final Repo repo;
	private final YandexDtoMapper mapper;

	@Override
	public List<RR> getSubdomainsList() {
		List<RR> subdomainsList = getSubdomainsList0();
		repo.saveDomains(subdomainsList, getDomainName());
		return subdomainsList;
	}

	private List<RR> getSubdomainsList0() {
		return client.get()
				.uri(uriBuilder -> uriBuilder
						.path(listRecordSetsUrl())
						.queryParam("filter", "{filter}")
						.build("type='A'"))
				.headers(this::bearerToken)
				.retrieve()
				.bodyToMono(ListRecordSetsResponse.class)
				.map(mapper::dnsResponseToRRs)
				.defaultIfEmpty(List.of())
				.block()
				.stream()
				.map(this::clearSubdomainName)
				.toList();
	}

	@Override
	public boolean addSubdomain(RR rr) {
		return TRUE.equals(client.post()
				.uri(updateRecordSetsUrl())
				.headers(this::bearerToken)
				.bodyValue(mapper.add(rr))
				.retrieve()
				.bodyToMono(UpdateDNSRecordsResponse.class)
				.map(YandexDNSClient::handleUpdateResponse)
				.block());
	}

	@Override
	public boolean deleteSubdomain(RR rr) {
		return TRUE.equals(client.post()
				.uri(updateRecordSetsUrl())
				.headers(this::bearerToken)
				.bodyValue(mapper.delete(rr))
				.retrieve()
				.bodyToMono(UpdateDNSRecordsResponse.class)
				.map(YandexDNSClient::handleUpdateResponse)
				.block());
	}

	private static boolean handleUpdateResponse(UpdateDNSRecordsResponse resp) {
		if (resp.getError() != null) {
			log.error("update subdomain error: {}", resp.getError().getMessage());
			return false;
		}
		return resp.getResponse() != null;
	}

	private String updateRecordSetsUrl() {
		return "/" + getDNSZoneId() + ":updateRecordSets";
	}

	private String listRecordSetsUrl() {
		return "/" + getDNSZoneId() + ":listRecordSets";
	}

	private void bearerToken(HttpHeaders h) {
		h.setBearerAuth(tokenService.getIAMToken());
	}

	private RR clearSubdomainName(RR rr) {
		String domain = rr.domain()
				.replaceAll("\\.$", "")
				.replace("." + getDomainName(), "");
		return rr.withDomain(domain);
	}
}
