package com.bazlur.crawler;

import org.jsoup.nodes.Document;

/**
 * @author Bazlur Rahman Rokon
 * @since 10/27/16.
 */
public interface PageProcessor {
	void process(Document document);
}
