package com.bazlur.crawler;

/**
 * @author Bazlur Rahman Rokon
 * @since 10/27/16.
 */
public class MainApp {
	public static void main(String[] args) {

		String urlToCrawl = "http://www.bazlur.com/";

		Crawler crawler = new Crawler(url -> url.startsWith(urlToCrawl));
		crawler.addUrl(urlToCrawl);
		crawler.addPageProcessors(new SaveOnDiskProcessor());
		crawler.crawl();
	}
}
