/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.crawler.web.robotstxt;

import com.qwazr.crawler.web.WebRequestDefinition;
import com.qwazr.crawler.web.driver.DriverInterface;
import com.qwazr.utils.LoggerUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class RobotsTxt {

	public enum Status {

		ERROR(false), NO_ROBOTSTXT(true), ALLOW(true), DISALLOW(false);

		public final boolean isCrawlable;

		Status(boolean isCrawlable) {
			this.isCrawlable = isCrawlable;
		}

	}

	private static final Logger logger = LoggerUtils.getLogger(RobotsTxt.class);

	private final RobotsTxtUserAgentMap userAgentMap;
	private final int httpStatusCode;
	private final long downloadTime;

	RobotsTxt(final InputStream input, final Charset charset) throws IOException {
		this.userAgentMap = RobotsTxtUserAgentMap.of(input, charset);
		this.httpStatusCode = 200;
		this.downloadTime = System.currentTimeMillis();
	}

	RobotsTxt(final int statusCode) {
		this.userAgentMap = null;
		this.httpStatusCode = statusCode;
		this.downloadTime = System.currentTimeMillis();
	}

	public boolean hasExpired(final TimeUnit unit, final int duration) {
		return (System.currentTimeMillis() - unit.toMillis(duration)) > downloadTime;
	}

	public Map<String, RobotsTxtClauseSet> getClausesMap() {
		return userAgentMap == null ? null : userAgentMap.clauseMap;
	}

	/**
	 * Build the Robots.txt URL from a reference URL
	 *
	 * @param uri the reference URI
	 * @return a Robots.txt URI
	 * @throws MalformedURLException if the URL is malformed
	 * @throws URISyntaxException    if the URI syntax is wrong
	 */
	public static URI getRobotsURI(final URI uri) throws URISyntaxException {
		StringBuilder sb = new StringBuilder();
		sb.append(uri.getScheme());
		sb.append("://");
		sb.append(uri.getHost());
		if (uri.getPort() != -1) {
			sb.append(':');
			sb.append(uri.getPort());
		}
		sb.append("/robots.txt");
		return new URI(sb.toString());
	}

	/**
	 * Return the status of the specified URL
	 *
	 * @param uri the URI to test against the Robots.txt rules
	 * @return the robotsTxt status
	 * @throws MalformedURLException if the URL is malformed
	 * @throws URISyntaxException    if the URI syntax is wrong
	 */
	public final Status getStatus(final URI uri, final String userAgent)
			throws MalformedURLException, URISyntaxException {
		final Status status = getStatusNoLogs(uri, userAgent);
		logger.info(() -> "Check robots.txt returns " + status.name() + " for " + uri);
		return status;
	}

	private Status getStatusNoLogs(final URI uri, final String userAgent) throws MalformedURLException {
		switch (httpStatusCode) {
		case 400:
		case 404:
			return Status.NO_ROBOTSTXT;
		case 200:
			break;
		default:
			return Status.ERROR;
		}
		if (userAgentMap == null)
			return Status.ALLOW;
		RobotsTxtClauseSet clauseSet = userAgentMap.get(userAgent.toLowerCase());
		if (clauseSet == null)
			clauseSet = userAgentMap.get("*");
		if (clauseSet == null)
			return Status.ALLOW;
		return clauseSet.isAllowed(uri.toURL().getFile()) ? Status.ALLOW : Status.DISALLOW;
	}

	public static RobotsTxt download(final DriverInterface driver, final URI uri) throws IOException {
		logger.info(() -> "Try to download robots.txt " + uri);
		try (final DriverInterface.Body get = driver.body(WebRequestDefinition.of(uri.toString()).build())) {
			final int sc = get.getResponseCode();
			if (sc != 200)
				return new RobotsTxt(sc);
			final DriverInterface.Content content = get.getContent();
			if (content == null)
				return new RobotsTxt(sc);
			final Charset charset = content.getCharset();
			try (final InputStream is = content.getInput()) {
				return new RobotsTxt(is, charset == null ? StandardCharsets.UTF_8 : charset);
			}
		}
	}
}