package main.bash;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ImageService {
	@Value("${bash.image.prepared.top.url}")
	private URI preparedImageTopUrl;
	@Value("${bash.image.prepared.bottom.url}")
	private URI preparedImageBottomUrl;

	private final Font font = new Font("sans-serif", Font.PLAIN, 16);
	private final Color textColor = new Color(25, 23, 23);
	private BufferedImage topImage;
	private BufferedImage bottomImage;

	@PostConstruct
	private void init() throws IOException {
		topImage = ImageIO.read(preparedImageTopUrl.toURL());
		bottomImage = ImageIO.read(preparedImageBottomUrl.toURL());
	}

	public InputStream buildQuotePhoto(String text) {
		return createTextImageFile(text);
	}

	private InputStream createTextImageFile(String text) {
		int wrapLength = 60;
		String wrapped = wrap(text, wrapLength);

		BufferedImage content = createTextImage(wrapped).getBufferedImage();
		return mergeImages(topImage, bottomImage, content);
	}

	private String wrap(String text, int wrapLength) {
		return Stream.of(text.split("\n"))
				.map(row -> {
					if (row.isBlank())
						return " ";
					if (row.length() < wrapLength)
						return row;
					return WordUtils.wrap(row, wrapLength);
				})
				.collect(Collectors.joining("\n"));
	}

	private ImagePlus createTextImage(String text) {
		ImagePlus image = createImageBox(600, getHeightByText(text));
		fillBackground(image);
		fillText(image, text);
		return image;
	}

	private int getHeightByText(String text) {
		long countLines = text.chars().filter(ch -> ch == '\n').count() + 1;
		return (int) (countLines * 22) + 15;
	}

	private ImagePlus createImageBox(int width, int height) {
		return IJ.createImage("image", width, height, 0, 24);
	}

	private void fillBackground(ImagePlus image) {
		ImageProcessor ip = image.getProcessor();
		ip.setColor(Color.WHITE);
		ip.fill();
	}

	private void fillText(ImagePlus image, String text) {
		ImageProcessor ip = image.getProcessor();
		ip.setColor(textColor);
		ip.setFont(font);
		ip.setAntialiasedText(true);
		ip.drawString(text, 25, 25);
	}


	private InputStream mergeImages(BufferedImage header, BufferedImage footer, BufferedImage imageContent) {
		BufferedImage result = mergeImages(mergeImages(header, imageContent), footer);

		return getPNGInputStream(result);
	}

	private BufferedImage mergeImages(BufferedImage top, BufferedImage bottom) {
		int w = Math.max(top.getWidth(), bottom.getWidth());
		int h = top.getHeight() + bottom.getHeight();
		BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		Graphics g = combined.getGraphics();
		g.drawImage(top, 0, 0, null);
		g.drawImage(bottom, 0, top.getHeight(), null);
		g.dispose();

		return combined;
	}

	private InputStream getPNGInputStream(BufferedImage image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", baos);
			return new ByteArrayInputStream(baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
