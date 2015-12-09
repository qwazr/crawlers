/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
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

import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LinkUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class RobotsTxt {

	private static final Logger logger = LoggerFactory.getLogger(RobotsTxt.class);

	private final UserAgentMap userAgentMap;
	private final String userAgent;
	private final int httpStatusCode;

	RobotsTxt(String userAgent, InputStream input) throws IOException {
		this.userAgent = userAgent;
		this.userAgentMap = new UserAgentMap(input);
		this.httpStatusCode = 200;
	}

	RobotsTxt(String userAgent, int statusCode) {
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
	static URI getRobotsURI(URI uri) throws MalformedURLException, URISyntaxException {
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

		private RobotsTxtStatus(boolean isCrawlable) {
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
	RobotsTxtStatus getStatus(URI uri) throws MalformedURLException, URISyntaxException {
		RobotsTxtStatus status = getStatusNoLogs(uri);
		if (logger.isInfoEnabled())
			logger.info("Check robots.txt returns " + status.name() + " for " + uri);
		return status;
	}

	private RobotsTxtStatus getStatusNoLogs(URI uri) throws MalformedURLException, URISyntaxException {
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

		public UserAgentMap(InputStream input) throws IOException {
			clauseMap = parseContent(input);
		}

		/**
		 * @param userAgent
		 * @return the right DisallowSet for the passed user-agent
		 */
		protected ClauseSet get(String userAgent) {
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
		private Map<String, ClauseSet> parseContent(InputStream input) throws IOException {
			Map<String, ClauseSet> clauseMap = new LinkedHashMap<>();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			try {
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
							clauseSet = new ClauseSet(userAgent);
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
			} finally {
				br.close();
			}
		}
	}

	private class ClauseSet {

		/**
		 * Contains the clause list of a "robots.txt" file for one "User-agent".
		 */

		private LinkedHashMap<String, Boolean> clauses;

		private ClauseSet(String userAgent) {
			clauses = null;
		}

		/**
		 * Add a Allow/Disallow clause
		 *
		 * @param clause the path of the clause
		 * @param allow  allow or disallow
		 */
		protected void add(String clause, Boolean allow) {
			synchronized (this) {
				if (clauses == null)
					clauses = new LinkedHashMap<String, Boolean>();
				clauses.put(clause, allow);
			}
		}

		/**
		 * @param path the path to check
		 * @return false if the URL is not allowed
		 */
		protected boolean isAllowed(String path) {
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

	static RobotsTxt download(WebCrawlDefinition.ProxyDefinition proxy, String userAgent, URI uri) throws IOException {
		InputStream is = null;
		try {
			Request request = Request.Get(uri.toString()).connectTimeout(60000).socketTimeout(60000);
			if (proxy != null) {
				if (proxy.http_proxy != null && !proxy.http_proxy.isEmpty())
					request = request.viaProxy(proxy.http_proxy);
				if ("https".equals(uri.getScheme()) && proxy.ssl_proxy != null && !proxy.ssl_proxy.isEmpty())
					request = request.viaProxy(proxy.ssl_proxy);
			}
			is = request.execute().returnContent().asStream();
			if (logger.isInfoEnabled())
				logger.info("Download robots.txt " + uri);
			return new RobotsTxt(userAgent, is);
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
		} finally {
			if (is != null)
				IOUtils.closeQuietly(is);
		}
	}

}