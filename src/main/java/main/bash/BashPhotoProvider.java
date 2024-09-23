package main.bash;

import lombok.RequiredArgsConstructor;
import main.bash.models.BashPhoto;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class BashPhotoProvider {
	private final ImageService imageService;

	public BashPhoto getByQuoteFile(QuoteFile quoteFile) {
		BashPhoto.BashPhotoBuilder builder = BashPhoto.builder()
				.quoteId(quoteFile.quoteId());
		if (quoteFile.fileId() != null) {
			builder.fileId(quoteFile.fileId());
		} else {
			InputStream photoIS = imageService.buildQuotePhoto(quoteFile.quote());
			builder.photoBytes(photoIS);
		}

		return builder.build();
	}
}
