package main.main_tech.docker_registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Images {
	List<Image> images;

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Image {
		String id;
		List<String> tags;
	}
}
