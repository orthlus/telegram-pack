package main.bash.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static org.springframework.web.util.HtmlUtils.htmlUnescape;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {
	private String text;
	private Integer rating;

	@JsonProperty
	public void setRating(Integer rating) {
		this.rating = rating == null ? 0 : rating;
	}

	@JsonProperty
	public void setText(String text) {
		String htmlUnescaped = htmlUnescape(htmlUnescape(text));

		String tag = "<div class=\"quote__strips\"";
		if (htmlUnescaped.contains(tag)) {
			htmlUnescaped = htmlUnescaped.split(tag)[0];
		}
		htmlUnescaped = htmlUnescaped.replaceAll("<br>", "\n");
		this.text = htmlUnescaped.trim();
	}
}
