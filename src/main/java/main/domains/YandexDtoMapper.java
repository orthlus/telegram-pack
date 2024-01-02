package main.domains;

import main.domains.common.RR;
import main.domains.common.dto.yandex.AdditionOrDeletion;
import main.domains.common.dto.yandex.ListRecordSetsResponse;
import main.domains.common.dto.yandex.UpdateDNSRecordsRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class YandexDtoMapper {
	public List<RR> map(List<AdditionOrDeletion> additionOrDeletions) {
		return additionOrDeletions.stream().map(this::map).toList();
	}

	public AdditionOrDeletion map(RR rr) {
		return AdditionOrDeletion.builder()
				.type("A")
				.ttl("600")
				.name(rr.domain())
				.data(List.of(rr.ip()))
				.build();
	}

	public RR map(AdditionOrDeletion aod) {
		return new RR(aod.getData().get(0), aod.getName());
	}

	public List<RR> dnsResponseToRRs(ListRecordSetsResponse listResponse) {
		return map(listResponse.getRecordSets());
	}

	public UpdateDNSRecordsRequest add(RR rr) {
		return UpdateDNSRecordsRequest.builder()
				.additions(List.of(map(rr)))
				.build();
	}

	public UpdateDNSRecordsRequest delete(RR rr) {
		return UpdateDNSRecordsRequest.builder()
				.deletions(List.of(map(rr)))
				.build();
	}
}
