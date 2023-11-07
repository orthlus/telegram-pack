package main.habr;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import main.habr.rss.RssFeed;

public interface HabrHttp {
	@RequestLine("GET {url}")
	String pageContent(@Param("url") String url);

	@RequestLine("GET /rss/{type}/?limit=100")
	@Headers("Accept: text/xml")
	RssFeed rss(@Param("type") String type);
}
