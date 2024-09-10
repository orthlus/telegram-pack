package main.bash.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static org.springframework.web.util.HtmlUtils.htmlUnescape;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {
	private String text;
	@JsonProperty
	private Integer rating;

	@JsonProperty
	public void setText(String text) {
		this.text = htmlUnescape(htmlUnescape(text));
	}
}
