/**
 * Copyright 2014-2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.crawler.web.manager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.script.ScriptException;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.crawler.web.driver.BrowserDriver;
import com.qwazr.crawler.web.driver.BrowserDriverBuilder;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.crawler.web.service.WebCrawlDefinition.EventEnum;
import com.qwazr.crawler.web.service.WebCrawlDefinition.Script;
import com.qwazr.crawler.web.service.WebCrawlStatus;
import com.qwazr.crawler.web.service.WebCrawlStatus.UrlStatus;
import com.qwazr.job.script.ScriptManager;
import com.qwazr.job.script.ScriptRunThread;
import com.qwazr.utils.server.ServerException;

public class WebCrawlThread extends Thread {

	private static final Logger logger = LoggerFactory
			.getLogger(WebCrawlThread.class);

	private final CurrentSession session;
	final WebCrawlDefinition crawlDefinition;

	private final List<Matcher> parametersMatcherList;
	private final List<Matcher> exclusionMatcherList;
	private final List<Matcher> inclusionMatcherList;

	private BrowserDriver<?> driver = null;

	WebCrawlThread(ThreadGroup threadGroup, String sessionName,
			WebCrawlDefinition crawlDefinition) throws ServerException {
		super(threadGroup, "oss-web-crawler " + sessionName);
		this.session = new CurrentSession(sessionName,
				crawlDefinition.variables);
		this.crawlDefinition = crawlDefinition;
		if (crawlDefinition.browser_type == null)
			throw new ServerException(Status.NOT_ACCEPTABLE,
					"The browser_type is missing");
		if (crawlDefinition.entry_url == null)
			throw new ServerException(Status.NOT_ACCEPTABLE,
					"The entry_url is missing");
		parametersMatcherList = getMatcherList(crawlDefinition.parameters_patterns);
		exclusionMatcherList = getMatcherList(crawlDefinition.exclusion_patterns);
		inclusionMatcherList = getMatcherList(crawlDefinition.inclusion_patterns);
	}

	private final static List<Matcher> getMatcherList(List<String> patternList)
			throws ServerException {
		if (patternList == null || patternList.isEmpty())
			return null;
		try {
			List<Matcher> matcherList = new ArrayList<Matcher>(
					patternList.size());
			for (String pattern : patternList) {
				Matcher matcher = Pattern.compile(pattern).matcher(
						StringUtils.EMPTY);
				matcherList.add(matcher);
			}
			return matcherList;
		} catch (PatternSyntaxException e) {
			throw new ServerException(Status.NOT_ACCEPTABLE, e.getMessage());
		}
	}

	String getSessionName() {
		return session.getName();
	}

	WebCrawlStatus getStatus() {
		return new WebCrawlStatus(ClusterManager.INSTANCE.myAddress,
				crawlDefinition.entry_url, session.getStartTime(),
				this.getState(), new UrlStatus(session));
	}

	void abort() {
		session.abort();
	}

	private final static boolean checkMatcher(String value,
			List<Matcher> matcherList) {
		for (Matcher matcher : matcherList) {
			matcher.reset(value);
			if (matcher.find())
				return true;
		}
		return false;
	}

	/**
	 * Check the inclusion list. Returns TRUE if the inclusion list is empty.
	 * 
	 * @param uriString
	 * @return
	 */
	private boolean checkInclusion(String uriString) {
		if (inclusionMatcherList == null || inclusionMatcherList.isEmpty())
			return true;
		return checkMatcher(uriString, inclusionMatcherList);
	}

	/**
	 * Check the exclusion list. Returns FALSE if the exclusion list is empty.
	 * 
	 * @param uriString
	 * @return
	 */
	private boolean checkExclusion(String uriString) {
		if (exclusionMatcherList == null || exclusionMatcherList.isEmpty())
			return false;
		return checkMatcher(uriString, exclusionMatcherList);
	}

	private boolean checkPatterns(String uriString) {
		return checkInclusion(uriString) && !checkExclusion(uriString);
	}

	/**
	 * Remove the fragment if remove_framents is set to true
	 * 
	 * @param uriBuilder
	 */
	private void checkRemoveFragment(URIBuilder uriBuilder) {
		if (crawlDefinition.remove_fragments == null
				|| !crawlDefinition.remove_fragments)
			return;
		uriBuilder.setFragment(null);
	}

	/**
	 * Remove any query parameter which match the parameters_matcher list
	 * 
	 * @param uriBuilder
	 */
	private void checkRemoveParameter(URIBuilder uriBuilder) {
		if (parametersMatcherList == null || parametersMatcherList.isEmpty())
			return;
		List<NameValuePair> oldParams = uriBuilder.getQueryParams();
		if (oldParams == null || oldParams.isEmpty())
			return;
		uriBuilder.clearParameters();
		for (NameValuePair param : oldParams)
			if (!checkMatcher(param.getName() + "=" + param.getValue(),
					parametersMatcherList))
				uriBuilder.addParameter(param.getName(), param.getValue());
	}

	/**
	 * Remove the fragment and the query parameters following the configuration
	 * 
	 * @param uri
	 * @return
	 */
	private URI checkLink(URI uri) {
		URIBuilder uriBuilder = new URIBuilder(uri);
		checkRemoveFragment(uriBuilder);
		checkRemoveParameter(uriBuilder);
		try {
			return uriBuilder.build();
		} catch (URISyntaxException e) {
			logger.warn(e.getMessage(), e);
			return null;
		}
	}

	private void crawl(CurrentURI currentURI) {

		URI uri = currentURI.getInitialUri();
		String uriString = uri.toString();
		session.setCurrentUri(uriString);

		// Check if the URL is well formated
		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme)
				&& !"https".equalsIgnoreCase(scheme)) {
			session.incIgnoredCount();
			currentURI.setIgnored();
			logger.info("Ignored (not http) " + uri);
			return;
		}

		// Check with exclusion/inclusion list
		if (!checkPatterns(uriString)) {
			session.incIgnoredCount();
			currentURI.setIgnored();
			logger.info("Ignored (pattern lists) " + uri);
			return;
		}

		// Load the URL
		logger.info("Crawling " + uri);
		driver.get(uriString);

		try {
			uri = new URI(uriString);
			currentURI.setFinalURI(uri);
		} catch (URISyntaxException e) {
			session.incErrorCount();
			currentURI.setError(e);
			return;
		}

		// Check again with exclusion/inclusion list
		// in case of redirection
		uriString = driver.getCurrentUrl();
		if (!checkPatterns(uriString)) {
			session.incIgnoredCount();
			currentURI.setIgnored();
			logger.info("Ignored (pattern lists after redirection) " + uri);
			return;
		}

		session.incCrawledCount();
		currentURI.setCrawled();

		// Let's look for the a tags
		List<WebElement> links = driver.findElements(By.tagName("a"));
		if (links == null || links.isEmpty())
			return;

		// Building the URI list
		Map<String, URI> linkMap = new TreeMap<String, URI>();
		for (WebElement link : links) {
			String href = link.getAttribute("href");
			if (href == null)
				continue;
			try {
				URI linkURI = URIUtils.resolve(uri, href);
				if (linkURI != null) {
					linkURI = checkLink(linkURI);
					if (linkURI != null)
						linkMap.put(linkURI.toString(), linkURI);
				}
			} catch (IllegalArgumentException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		currentURI.setLinks(linkMap.values());
	}

	private void crawl(Set<URI> crawledURIs, URI uri, int depth)
			throws ServerException {
		if (session.isAborting())
			return;

		// Check if it has been already crawled
		if (crawledURIs != null) {
			if (crawledURIs.contains(uri))
				return;
			crawledURIs.add(uri);
		}

		CurrentURI currentURI = new CurrentURI(uri, depth);

		script(EventEnum.before_crawl, currentURI);
		if (!currentURI.isIgnored())
			crawl(currentURI);
		script(EventEnum.after_crawl, currentURI);

		// Check if we reach the max depth
		depth = currentURI.getDepth() + 1;
		if (crawlDefinition.max_depth == null
				|| depth >= crawlDefinition.max_depth)
			return;

		// Let's crawl the childs
		if (currentURI.getLinks() != null)
			for (URI uriLink : currentURI.getLinks())
				crawl(crawledURIs, uriLink, depth);

	}

	/**
	 * Execute the script related to the passed event.
	 * 
	 * @param event
	 * @param currentCrawl
	 * @throws ServerException
	 */
	private void script(EventEnum event, CurrentURI currentURI)
			throws ServerException {
		if (crawlDefinition.scripts == null)
			return;
		Script script = crawlDefinition.scripts.get(event);
		if (script == null)
			return;
		Map<String, Object> objects = new TreeMap<String, Object>();
		objects.put("session", session);
		if (script.variables != null)
			objects.putAll(script.variables);
		if (driver != null)
			objects.put("driver", driver);
		if (currentURI != null)
			objects.put("current", currentURI);
		ScriptRunThread scriptRunThread = ScriptManager.INSTANCE.runSync(
				script.name, objects);
		if (scriptRunThread.getException() != null)
			throw new ServerException(scriptRunThread.getException());
	}

	private void runner() throws InstantiationException, URISyntaxException,
			IOException, ScriptException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ServerException {
		try {
			driver = new BrowserDriverBuilder(crawlDefinition).build();
			script(EventEnum.before_session, null);
			Set<URI> crawledURIs = new HashSet<URI>();
			crawl(crawledURIs, new URI(crawlDefinition.entry_url), 0);
		} finally {
			if (driver != null)
				driver.close();
			script(EventEnum.after_session, null);
		}
	}

	@Override
	final public void run() {
		try {
			runner();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			WebCrawlerManager.INSTANCE.removeSession(this);
		}

	}
}
