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

import com.qwazr.utils.LinkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class RobotsTxt {

	private final UserAgentMap userAgentMap;

	 RobotsTxt(InputStream input) {
		this.userAgentMap = new UserAgentMap(input);
	}

	/**
	 * Construit l'URL d'accès au fichier robots.txt à partir d'une URL donnée
	 *
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static URL getRobotsUrl(URL url) throws MalformedURLException, URISyntaxException {
		StringBuilder sb = new StringBuilder();
		sb.append(url.getProtocol());
		sb.append("://");
		sb.append(url.getHost());
		if (url.getPort() != -1) {
			sb.append(':');
			sb.append(url.getPort());
		}
		sb.append("/robots.txt");
		return LinkUtils.newEncodedURL(sb.toString());
	}

	public enum RobotsTxtStatus {
		ERROR, NO_ROBOTSTXT, ALLOW, DISALLOW;
	}

	/**
	 * Return the status of the specified URL
	 *
	 * @param url
	 * @param userAgent
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	RobotsTxtStatus getStatus(String userAgent, URL url, Integer responseCode)
					throws MalformedURLException, URISyntaxException {
		if (responseCode == null)
			return RobotsTxtStatus.ERROR;
		switch (responseCode) {
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
		if (clauseSet.isAllowed(url.getFile()))
			return RobotsTxtStatus.ALLOW;
		return RobotsTxtStatus.DISALLOW;
	}

	/**
	 * Contains the list of clauses of a "robots.txt" file *
	 */
	public class UserAgentMap {

		private final Map<String, ClauseSet> clauseMap;

		public UserAgentMap(InputStream input) {
			clauseMap = null;
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
		private void parseContent(InputStream input) throws IOException {
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
}