package main.bash;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class BashPhotoProvider {
	private final ImageService imageService;

	public String getPhotoId(QuoteFile quoteFile) {
		return quoteFile.fileId();
	}

	public InputStream buildPhoto(QuoteFile quoteFile) {
		return imageService.buildQuotePhoto(quoteFile.quote());
	}

	public boolean existsPhoto(QuoteFile quoteFile) {
		return quoteFile.fileId() != null;
	}
}
