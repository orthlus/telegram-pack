package main.main_tech;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public abstract class S3Client {
	public AmazonS3 client(String id, String key, String url, String region) {
		BasicAWSCredentials credentials = new BasicAWSCredentials(id, key);
		AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(credentials);
		var configuration = new AmazonS3ClientBuilder.EndpointConfiguration(url, region);
		return AmazonS3ClientBuilder.standard()
				.withCredentials(provider)
				.withEndpointConfiguration(configuration)
				.build();
	}
}
