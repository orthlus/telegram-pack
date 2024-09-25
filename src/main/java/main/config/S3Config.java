package main.config;

import art.aelaort.DefaultS3Params;
import art.aelaort.S3Params;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
	@Bean
	public S3Params bashS3Params(
			@Value("${bash.s3.url}") String url,
			@Value("${bash.s3.region}") String region,
			@Value("${bash.s3.id}") String id,
			@Value("${bash.s3.key}") String key
	) {
		return new DefaultS3Params(id, key, url, region);
	}
}
