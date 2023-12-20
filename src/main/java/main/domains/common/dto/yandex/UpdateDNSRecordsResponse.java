package main.domains.common.dto.yandex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import main.domains.common.dto.yandex.base.ErrorDto;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateDNSRecordsResponse {
	@JsonProperty
	ErrorDto error;
	@JsonProperty
	Object response;
}
