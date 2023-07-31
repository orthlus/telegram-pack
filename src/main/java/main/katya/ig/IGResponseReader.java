package main.katya.ig;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import main.katya.ig.entity.IGMedia;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class IGResponseReader {
	private final ObjectMapper json = new ObjectMapper();

	public String getUrlFromSingleMedia(String jsonStr) {
		Optional<IGMedia> igMedia = parseMedia(jsonStr);
		if (igMedia.isPresent()) {
			return extractSingleMediaUrl(igMedia.get());
		} else {
			return "";
		}
	}

	public Optional<IGMedia> parseMedia(String jsonStr) {
		try {
			return Optional.of(json.readValue(jsonStr, IGMedia.class));
		} catch (IOException e) {
			log.error("json parse error, json - {}", jsonStr, e);
			return Optional.empty();
		}
	}

	public String extractSingleMediaUrl(IGMedia media) {
		String s = "";
		switch (media.getMediaType()) {
			case 1 -> s = media.getSinglePhotoUrl();
			case 2 -> s = media.getSingleVideoUrl();
		}
		return s;
	}
}
