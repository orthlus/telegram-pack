package main.domains;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.domains.common.RR;
import main.domains.common.dto.yandex.ListRecordSetsResponse;
import main.domains.common.dto.yandex.UpdateDNSRecordsRequest;
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
				.map(mapper::map)
				.block();
	}

	@Override
	public boolean addSubdomain(RR rr) {
		return TRUE.equals(client.post()
				.uri(updateRecordSetsUrl())
				.headers(this::bearerToken)
				.body(mapper.add(rr), UpdateDNSRecordsRequest.class)
				.retrieve()
				.bodyToMono(UpdateDNSRecordsResponse.class)
				.map(YandexDNSClient::updateResponseHandling)
				.block());
	}

	@Override
	public boolean deleteSubdomain(RR rr) {
		return TRUE.equals(client.post()
				.uri(updateRecordSetsUrl())
				.headers(this::bearerToken)
				.body(mapper.delete(rr), UpdateDNSRecordsRequest.class)
				.retrieve()
				.bodyToMono(UpdateDNSRecordsResponse.class)
				.map(YandexDNSClient::updateResponseHandling)
				.block());
	}

	private static boolean updateResponseHandling(UpdateDNSRecordsResponse resp) {
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
}
