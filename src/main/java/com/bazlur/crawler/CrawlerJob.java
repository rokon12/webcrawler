package com.bazlur.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

/**
 * @author Bazlur Rahman Rokon
 * @since 10/27/16.
 */
public class CrawlerJob implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerJob.class);

	private Crawler crawler;
	private String url;
	private List<PageProcessor> pageProcessors;

	public CrawlerJob(Crawler crawler, String url, List<PageProcessor> pageProcessors) {
		this.crawler = crawler;
		this.url = url;
		this.pageProcessors = pageProcessors;
	}

	public CrawlerJob(Crawler crawler, String url) {
		this.crawler = crawler;
		this.url = url;
	}

	@Override
	public void run() {
		try {
			crawl();
		} catch (IOException e) {
			LOGGER.error("Couldn't crow url :{}", e);
		}
	}

	private void crawl() throws IOException {
		URL url = new URL(this.url);
		URLConnection urlConnection = url.openConnection();

		try (InputStream input = urlConnection.getInputStream()) {
			Document doc = Jsoup.parse(input, "UTF-8", "");
			Elements elements = doc.select("a");
			String baseUrl = url.toExternalForm();

			for (Element element : elements) {
				String linkUrl = element.attr("href");
				String normalizedUrl = UrlNormalizer.normalize(linkUrl, baseUrl);
				crawler.linksQueue.put(normalizedUrl);
			}

			for (PageProcessor pageProcessor : pageProcessors) {
				pageProcessor.process(doc);
			}

			if (crawler.barrier.getNumberWaiting() == 1) {
				crawler.barrier.await();
			}
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException("Error connecting to URL", e);
		}
	}
}
