package main;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("billing")
public class RestTemplateProperties {
	private String timewebUrl;
	private String timewebToken;
	private String instagramUrl;
	private String instagramToken;
	private String tiktokUrl;
	private String tiktokToken;
}
