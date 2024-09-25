package main.bash.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.web.util.HtmlUtils.htmlUnescape;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {
	private String text;
	private Integer rating;
	private LocalDate date;
	@JsonProperty
	private Integer id;

	@JsonProperty
	public void setDate(String date) {
		if (date.contains(" ")) {
			this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy'T' H:mm"));
		} else {
			this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm"));
		}
	}

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
