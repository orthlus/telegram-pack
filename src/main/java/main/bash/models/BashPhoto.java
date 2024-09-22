package main.bash.models;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Builder
@Getter
public class BashPhoto {
	private Integer quoteId;
	private String fileId;
	private InputStream photoBytes;

	@Override
	public String toString() {
		return "BashPhoto{fileId='%s', quoteId=%d}".formatted(fileId, quoteId);
	}
}
