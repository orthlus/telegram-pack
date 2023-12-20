package main.domains.common.dto.yandex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public class UpdateDNSRecordsRequest {
	@JsonProperty
	List<AdditionOrDeletion> deletions;
	@JsonProperty
	List<AdditionOrDeletion> additions;
}
