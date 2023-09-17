package main.main_tech.monitoring;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;

public class S3Client {
	@Value("${main_tech.monitoring.storage.id}")
	private String uploaderId;
	@Value("${main_tech.monitoring.storage.key}")
	private String uploaderKey;
	@Value("${main_tech.monitoring.storage.url}")
	private String url;
	@Value("${main_tech.monitoring.storage.region}")
	private String region;

	private AmazonS3 client() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(uploaderId, uploaderKey);
		AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(credentials);
		var configuration = new AmazonS3ClientBuilder.EndpointConfiguration(url, region);
		return AmazonS3ClientBuilder.standard()
				.withCredentials(provider)
				.withEndpointConfiguration(configuration)
				.build();
	}
}
