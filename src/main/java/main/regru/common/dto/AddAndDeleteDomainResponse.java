package main.regru.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddAndDeleteDomainResponse {
	@JsonProperty
	public String result;
	@JsonProperty
	public Answer answer;

	public boolean isResultSuccess() {
		String success = "success";
		return result.equals(success) && answer.domains.get(0).result.equals(success);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Answer {
		@JsonProperty
		public List<Domain> domains;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Domain {
		@JsonProperty
		public String result;
		@JsonProperty
		public String dname;
	}
}
