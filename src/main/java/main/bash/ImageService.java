package main.bash;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import main.bash.models.QuoteFile;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ImageService {
	@Value("${bash.image.prepared.bottom.url}")
	private URI preparedImageBottomUrl;

	private final Font textFont = new Font("sans-serif", Font.PLAIN, 14);
	private final Color textColor = new Color(25, 23, 23);

	private final Font topIdFont = new Font("sans-serif", Font.BOLD, 12);
	private final Color topIdColor = new Color(111, 155, 0);

	private final Font topDateFont = new Font("sans-serif", Font.PLAIN, 12);
	private final Color topDateColor = new Color(136, 128, 145);

	private final Font ratingFont = new Font("sans-serif", Font.PLAIN, 12);
	private final Color ratingColor = Color.BLACK;

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private ImagePlus bottomImage;

	@PostConstruct
	private void init() throws IOException {
		byte[] bytes = new RestTemplate().getForObject(preparedImageBottomUrl, byte[].class);
		String path = "/tmp/" + UUID.randomUUID();
		Files.write(Path.of(path), bytes);
		bottomImage = IJ.openImage(path);
	}

	public InputStream buildQuotePhoto(QuoteFile quoteFile) {
		return createTextImageFile(quoteFile.quote(),
				quoteFile.quoteOriginalId(),
				quoteFile.quoteDate().format(dateTimeFormatter),
				quoteFile.quoteRating());
	}

	private InputStream createTextImageFile(String text, int id, String date, int rating) {
		int wrapLength = 43;
		String wrapped = wrap(text, wrapLength);

		BufferedImage content = createTextImage(wrapped).getBufferedImage();
		BufferedImage top = createTopImage(id, date).getBufferedImage();
		BufferedImage bottom = createBottomImage(rating).getBufferedImage();
		return mergeImages(top, bottom, content);
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

	private ImagePlus createBottomImage(int rating) {
		ImagePlus image = bottomImage.duplicate();
		fillTextCenter(image, String.valueOf(rating), 208, 49, ratingFont, ratingColor);
		return image;
	}

	private ImagePlus createTopImage(int id, String date) {
		ImagePlus image = createImageBox(400, 50);
		fillBackground(image);
		fillText(image, date, 312, 33, topDateFont, topDateColor);
		fillText(image, "#" + id, 24, 34, topIdFont, topIdColor);
		return image;
	}

	private ImagePlus createTextImage(String text) {
		ImagePlus image = createImageBox(400, getHeightByText(text));
		fillBackground(image);
		fillText(image, text, 25, 25, textFont, textColor);
		return image;
	}

	private int getHeightByText(String text) {
		long countLines = text.chars().filter(ch -> ch == '\n').count() + 1;
		return (int) (countLines * 20) + 15;
	}

	private ImagePlus createImageBox(int width, int height) {
		return IJ.createImage("image", width, height, 0, 24);
	}

	private void fillBackground(ImagePlus image) {
		ImageProcessor ip = image.getProcessor();
		ip.setColor(Color.WHITE);
		ip.fill();
	}

	private void fillText(ImagePlus image, String text, int x, int y, Font font, Color textColor) {
		ImageProcessor ip = image.getProcessor();
		ip.setColor(textColor);
		ip.setFont(font);
		ip.setAntialiasedText(true);
		ip.drawString(text, x, y);
	}

	private void fillTextCenter(ImagePlus image, String text, int x, int y, Font font, Color textColor) {
		ImageProcessor ip = image.getProcessor();
		ip.setColor(textColor);
		ip.setFont(font);
		ip.setAntialiasedText(true);
		ip.setJustification(ImageProcessor.CENTER_JUSTIFY);
		ip.drawString(text, x, y);
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

	public InputStream getPNGInputStream(BufferedImage image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", baos);
			return new ByteArrayInputStream(baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
		BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = resizedImage.createGraphics();
		graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
		graphics2D.dispose();
		return resizedImage;
	}
}
