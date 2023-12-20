package main.domains;

import main.domains.common.RR;
import main.domains.common.dto.yandex.AdditionOrDeletion;
import main.domains.common.dto.yandex.ListRecordSetsResponse;
import main.domains.common.dto.yandex.UpdateDNSRecordsRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface YandexDtoMapper {
	default RR map(AdditionOrDeletion aod) {
		return new RR(aod.getData().get(0), aod.getName());
	}

	List<RR> map(List<AdditionOrDeletion> additionOrDeletions);

	default List<RR> map(ListRecordSetsResponse listResponse) {
		return map(listResponse.getRecordSets());
	}


	@Mapping(target = "type", constant = "A")
	@Mapping(target = "ttl", constant = "600")
	@Mapping(target = "name", source = "domain")
	@Mapping(target = "data", source = "ip")
	AdditionOrDeletion map(RR rr);

	default List<String> strings(String s) {
		return List.of(s);
	}

	default List<AdditionOrDeletion> toList(AdditionOrDeletion additionOrDeletion) {
		return List.of(additionOrDeletion);
	}

	@Mapping(target = "deletions", ignore = true)
	@Mapping(target = "additions", expression = "java(toList(addition))")
	UpdateDNSRecordsRequest add(AdditionOrDeletion addition);

	@Mapping(target = "additions", ignore = true)
	@Mapping(target = "deletions", expression = "java(toList(deletion))")
	UpdateDNSRecordsRequest delete(AdditionOrDeletion deletion);

	@Mapping(target = "deletions", ignore = true)
	@Mapping(target = "additions", source = "rr")
	UpdateDNSRecordsRequest add(RR rr);

	@Mapping(target = "additions", ignore = true)
	@Mapping(target = "deletions", source = "rr")
	UpdateDNSRecordsRequest delete(RR rr);
}
