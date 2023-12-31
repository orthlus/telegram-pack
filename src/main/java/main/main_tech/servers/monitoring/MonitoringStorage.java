package main.main_tech.servers.monitoring;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import main.main_tech.S3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
public class MonitoringStorage extends S3Client {
	@Value("${main_tech.monitoring.storage.id}")
	private String uploaderId;
	@Value("${main_tech.monitoring.storage.key}")
	private String uploaderKey;
	@Value("${main_tech.storage.url}")
	private String url;
	@Value("${main_tech.storage.region}")
	private String region;

	public void uploadFileContent(String bucket, String fileId, String fileContent) {
		try {
			AmazonS3 s3 = client();
			TransferManager tm = TransferManagerBuilder.standard()
					.withS3Client(s3)
					.build();
			Path file = Path.of("/tmp/"+ UUID.randomUUID());
			Files.writeString(file, fileContent);
			Upload upload = tm.upload(bucket, fileId, file.toFile());
			upload.waitForCompletion();
			tm.shutdownNow();
			s3.shutdown();
			Files.deleteIfExists(file);
		} catch (InterruptedException | IOException e) {
			log.error("s3 error - upload file {}/{}", bucket, fileId, e);
			throw new RuntimeException(e);
		}
	}

	public String getFileContent(String bucket, String fileId) {
		try {
			AmazonS3 s3 = client();
			TransferManager tm = TransferManagerBuilder.standard()
					.withS3Client(s3)
					.build();
			Path file = Path.of("/tmp/"+ UUID.randomUUID());
			Download download = tm.download(bucket, fileId, file.toFile());
			download.waitForCompletion();
			tm.shutdownNow();
			s3.shutdown();
			String result = Files.readString(file);
			Files.deleteIfExists(file);

			return result;
		} catch (InterruptedException | IOException e) {
			log.error("s3 error - get file {}/{}", bucket, fileId, e);
			throw new RuntimeException(e);
		}
	}

	private AmazonS3 client() {
		return client(uploaderId, uploaderKey, url, region);
	}
}
