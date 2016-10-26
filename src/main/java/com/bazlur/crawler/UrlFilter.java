package com.bazlur.crawler;

/**
 * @author Bazlur Rahman Rokon
 * @since 10/27/16.
 */

@FunctionalInterface
interface UrlFilter {
	boolean include(String url);
}
