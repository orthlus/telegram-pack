package main.katya.ig.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class IGMedia {
	@JsonProperty("media_type") // 1, 2
	int mediaType;
	@JsonProperty("thumbnail_url")
	String singlePhotoUrl;
	@JsonProperty("video_url")
	String singleVideoUrl;
}
