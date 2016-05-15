/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
 **/
package com.qwazr.crawler.web.manager;

import com.qwazr.crawler.web.driver.BrowserDriver;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.utils.CharsetUtils;
import com.qwazr.utils.LinkUtils;
import com.qwazr.utils.http.HttpUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class RobotsTxt {

	private static final Logger logger = LoggerFactory.getLogger(RobotsTxt.class);

	private final UserAgentMap userAgentMap;
	private final String userAgent;
	private final int httpStatusCode;

	RobotsTxt(final String userAgent, final InputStream input, final Charset charset) throws IOException {
		this.userAgent = userAgent;
		this.userAgentMap = new UserAgentMap(input, charset);
		this.httpStatusCode = 200;
	}

	RobotsTxt(final String userAgent, final int statusCode) {
		this.userAgent = userAgent;
		this.userAgentMap = null;
		this.httpStatusCode = statusCode;
	}

	/**
	 * Construit l'URL d'accès au fichier robots.txt à partir d'une URL donnée
	 *
	 * @param uri
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	static URI getRobotsURI(final URI uri) throws MalformedURLException, URISyntaxException {
		StringBuilder sb = new StringBuilder();
		sb.append(uri.getScheme());
		sb.append("://");
		sb.append(uri.getHost());
		if (uri.getPort() != -1) {
			sb.append(':');
			sb.append(uri.getPort());
		}
		sb.append("/robots.txt");
		return LinkUtils.newEncodedURI(sb.toString());
	}

	public enum RobotsTxtStatus {

		ERROR(false), NO_ROBOTSTXT(true), ALLOW(true), DISALLOW(false);

		final boolean isCrawlable;

		RobotsTxtStatus(boolean isCrawlable) {
			this.isCrawlable = isCrawlable;
		}

		boolean isCrawlable() {
			return isCrawlable;
		}
	}

	/**
	 * Return the status of the specified URL
	 *
	 * @param uri
	 * @return the robotsTxt status
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	final RobotsTxtStatus getStatus(final URI uri) throws MalformedURLException, URISyntaxException {
		RobotsTxtStatus status = getStatusNoLogs(uri);
		if (logger.isInfoEnabled())
			logger.info("Check robots.txt returns " + status.name() + " for " + uri);
		return status;
	}

	private RobotsTxtStatus getStatusNoLogs(final URI uri) throws MalformedURLException, URISyntaxException {
		switch (httpStatusCode) {
		case 400:
		case 404:
			return RobotsTxtStatus.NO_ROBOTSTXT;
		case 200:
			break;
		default:
			return RobotsTxtStatus.ERROR;
		}
		if (userAgentMap == null)
			return RobotsTxtStatus.ALLOW;
		ClauseSet clauseSet = userAgentMap.get(userAgent.toLowerCase());
		if (clauseSet == null)
			clauseSet = userAgentMap.get("*");
		if (clauseSet == null)
			return RobotsTxtStatus.ALLOW;
		if (clauseSet.isAllowed(uri.toURL().getFile()))
			return RobotsTxtStatus.ALLOW;
		return RobotsTxtStatus.DISALLOW;
	}

	/**
	 * Contains the list of clauses of a "robots.txt" file *
	 */
	public class UserAgentMap {

		private final Map<String, ClauseSet> clauseMap;

		public UserAgentMap(final InputStream input, final Charset charset) throws IOException {
			clauseMap = parseContent(input, charset);
		}

		/**
		 * @param userAgent
		 * @return the right DisallowSet for the passed user-agent
		 */
		final protected ClauseSet get(final String userAgent) {
			synchronized (this) {
				if (clauseMap == null)
					return null;
				return clauseMap.get(userAgent);
			}
		}

		/**
		 * Parse a robots.txt file
		 *
		 * @param input
		 * @throws IOException
		 */
		private Map<String, ClauseSet> parseContent(final InputStream input, final Charset charset) throws IOException {
			final Map<String, ClauseSet> clauseMap = new LinkedHashMap<>();
			try (final BufferedReader br = new BufferedReader(new InputStreamReader(input, charset))) {
				String line;
				ClauseSet currentClauseSet = null;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.startsWith("#"))
						continue;
					if (line.length() == 0)
						continue;
					StringTokenizer st = new StringTokenizer(line, ":");
					if (!st.hasMoreTokens())
						continue;
					String key = st.nextToken().trim();
					String value = null;
					if (!st.hasMoreTokens())
						continue;
					value = st.nextToken().trim();
					if ("User-agent".equalsIgnoreCase(key)) {
						String userAgent = value.toLowerCase();
						ClauseSet clauseSet = clauseMap.get(userAgent);
						if (clauseSet == null) {
							clauseSet = new ClauseSet();
							clauseMap.put(userAgent, clauseSet);
						}
						currentClauseSet = clauseSet;
					} else if ("Disallow".equalsIgnoreCase(key)) {
						if (currentClauseSet != null)
							currentClauseSet.add(value, false);
					} else if ("Allow".equalsIgnoreCase(key)) {
						if (currentClauseSet != null)
							currentClauseSet.add(value, true);
					}
				}
				return clauseMap;
			}
		}
	}

	private class ClauseSet {

		/**
		 * Contains the clause list of a "robots.txt" file for one "User-agent".
		 */

		private LinkedHashMap<String, Boolean> clauses;

		private ClauseSet() {
			clauses = null;
		}

		/**
		 * Add a Allow/Disallow clause
		 *
		 * @param clause the path of the clause
		 * @param allow  allow or disallow
		 */
		final protected void add(final String clause, final Boolean allow) {
			synchronized (this) {
				if (clauses == null)
					clauses = new LinkedHashMap<>();
				clauses.put(clause, allow);
			}
		}

		/**
		 * @param path the path to check
		 * @return false if the URL is not allowed
		 */
		final protected boolean isAllowed(String path) {
			synchronized (this) {
				if (clauses == null)
					return true;
				if ("".equals(path))
					path = "/";
				for (Map.Entry<String, Boolean> clause : clauses.entrySet())
					if (path.startsWith(clause.getKey()))
						return clause.getValue();
				return true;
			}
		}
	}

	static RobotsTxt download(final WebCrawlDefinition.ProxyDefinition proxy, final String userAgent, final URI uri)
			throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		try (final CloseableHttpClient httpClient = HttpUtils.createHttpClient_AcceptsUntrustedCerts()) {
			final Executor executor = Executor.newInstance(httpClient);
			Request request =
					Request.Get(uri.toString()).addHeader("Connection", "close").addHeader("User-Agent", userAgent)
							.connectTimeout(60000).socketTimeout(60000);
			request = BrowserDriver.applyProxy(proxy, uri, request);

			if (logger.isInfoEnabled())
				logger.info("Try to download robots.txt " + uri);
			return executor.execute(request).handleResponse(response -> {
				final StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300)
					throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
				final HttpEntity entity = response.getEntity();
				if (entity == null)
					throw new ClientProtocolException("Response contains no content");
				final ContentType contentType = ContentType.getOrDefault(entity);
				final Charset charset = contentType.getCharset();
				try (final InputStream is = entity.getContent()) {
					return new RobotsTxt(userAgent, is, charset == null ? CharsetUtils.CharsetUTF8 : charset);
				}
			});
		} catch (HttpResponseException e) {
			int sc = e.getStatusCode();
			if (sc != 404) {
				if (logger.isWarnEnabled())
					logger.warn("Get wrong status (" + sc + " code for: " + uri);
			} else {
				if (logger.isInfoEnabled())
					logger.info("Get wrong status (" + sc + " code for: " + uri);
			}
			return new RobotsTxt(userAgent, sc);
		}
	}

}