package main.bash;

import art.aelaort.S3Params;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.io.InputStream;

import static art.aelaort.S3ClientProvider.client;

@Component
@RequiredArgsConstructor
public class BashS3 {
	private final S3Params bashS3Params;
	@Value("${bash.s3.bucket}")
	private String bucket;

	public void uploadFile(InputStream is, String id) {
		try (S3Client client = client(bashS3Params)) {
			client.putObject(builder -> builder
							.key("files/" + id)
							.bucket(bucket)
							.build(),
					RequestBody.fromBytes(is.readAllBytes()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
