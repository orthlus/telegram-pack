package main.katya;

import feign.Param;
import feign.RequestLine;
import feign.Response;

public interface SocialApi {
	@RequestLine("GET /youtube/download?url={url}")
	Response youTubeFile(@Param("url") String url);

	@RequestLine("GET /tiktok/download?url={url}")
	Response tikTokFile(@Param("url") String url);
}
