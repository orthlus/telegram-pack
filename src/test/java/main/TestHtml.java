package main;

import org.springframework.web.util.HtmlUtils;

public class TestHtml {
	public static void main(String[] args) {
		String input = "&amp;lt;Hellcat&amp;gt; Настояший программер пьёт один раз в день - с утра и до вечера";
		System.out.println(HtmlUtils.htmlUnescape(input));
		System.out.println(HtmlUtils.htmlUnescape(HtmlUtils.htmlUnescape(input)));
	}
}
