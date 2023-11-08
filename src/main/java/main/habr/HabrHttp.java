package main.habr;

import feign.Param;
import feign.RequestLine;
import main.habr.rss.RssFeed;

import java.net.URI;

public interface HabrHttp {
	@RequestLine("GET /")
	String pageContent(URI uri);

	@RequestLine("GET /rss/{type}/?limit=100")
	RssFeed rss(@Param("type") String type);
}
