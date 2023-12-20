package main.domains.common.dto.yandex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListRecordSetsResponse {
	@JsonProperty
	List<AdditionOrDeletion> recordSets;
}
