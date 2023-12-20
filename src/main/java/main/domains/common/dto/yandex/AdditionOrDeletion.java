package main.domains.common.dto.yandex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdditionOrDeletion {
	@JsonProperty
	String name;
	@JsonProperty
	String type;
	@JsonProperty
	String ttl;
	@JsonProperty
	List<String> data;
}
