package com.bazlur.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * @author Bazlur Rahman Rokon
 * @since 10/27/16.
 */
public class Crawler {
	private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

	private static final String[] URL_STARTS_WITH = {
		"javascript:",
		"#"
	};

	private static final String[] URL_ENDS_WITH = {
		".swf",
		".pdf",
		".png",
		".gif",
		".jpg",
		".jpeg"
	};

	private UrlFilter urlFilter = null;
	private Set<String> crawledUrls = new HashSet<>();
	private ExecutorService crawlService;
	protected final LinkedBlockingQueue<String> linksQueue = new LinkedBlockingQueue<>();
	protected CyclicBarrier barrier = new CyclicBarrier(2);

	private List<PageProcessor> pageProcessors = new ArrayList<>();

	public void addPageProcessors(PageProcessor PageProcessor) {
		pageProcessors.add(PageProcessor);
	}

	public Crawler(UrlFilter urlFilter) {
		this.urlFilter = urlFilter;
	}

	public void addUrl(String url) {
		linksQueue.add(url);
	}

	public void crawl() {
		long startTime = System.currentTimeMillis();
		crawlService = Executors.newCachedThreadPool();

		int count = 0;

		while (!linksQueue.isEmpty()) {
			String nextUrl = null;
			try {
				nextUrl = linksQueue.take();
			} catch (InterruptedException e) {
				LOGGER.error("Couldn't get ulr from queue", e);
			}

			if (nextUrl == null) {
				LOGGER.info("Queue seems to be null here");
			}

			if (!shouldCrawlUrl(nextUrl)) {
				continue;
			}

			this.crawledUrls.add(nextUrl);

			if (!pageProcessors.isEmpty()) {
				CrawlerJob crawlerJob = new CrawlerJob(this, nextUrl, pageProcessors);
				crawlService.submit(crawlerJob);
			} else {
				CrawlerJob crawlerJob = new CrawlerJob(this, nextUrl);
				crawlService.submit(crawlerJob);
			}

			synchronized (this) {
				count++;
			}

			if (linksQueue.isEmpty()) {
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					LOGGER.error("Error crawling url", e);
				}
			}
		}

		crawlService.shutdown();

		try {
			crawlService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException ex) {
			LOGGER.error("Could not terminate crawler", ex);
		}

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;

		LOGGER.info("URL's crawled: {} in {} ms (avg: {})", count, totalTime, (totalTime / count));
	}

	private boolean shouldCrawlUrl(String nextUrl) {

		return !(this.urlFilter != null
			&& !this.urlFilter.include(nextUrl))
			&& !this.crawledUrls.contains(nextUrl)
			&& Stream.of(URL_STARTS_WITH).noneMatch(nextUrl::startsWith)
			&& Stream.of(URL_ENDS_WITH).noneMatch(nextUrl::startsWith);
	}
}
