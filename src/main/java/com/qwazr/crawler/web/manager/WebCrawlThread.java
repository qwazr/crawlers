/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.crawler.web.manager;

import com.google.common.net.InternetDomainName;
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
import com.qwazr.utils.WildcardMatcher;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class WebCrawlThread extends Thread {

	private static final Logger logger = LoggerFactory
			.getLogger(WebCrawlThread.class);

	private final CurrentSession session;
	final WebCrawlDefinition crawlDefinition;
	private final InternetDomainName internetDomainName;

	private final List<Matcher> parametersMatcherList;
	private final List<WildcardMatcher> exclusionMatcherList;
	private final List<WildcardMatcher> inclusionMatcherList;

	private BrowserDriver<?> driver = null;

	WebCrawlThread(ThreadGroup threadGroup, String sessionName,
				   WebCrawlDefinition crawlDefinition) throws ServerException {
		super(threadGroup, "oss-web-crawler " + sessionName);
		this.session = new CurrentSession(crawlDefinition, sessionName);
		this.crawlDefinition = crawlDefinition;
		if (crawlDefinition.browser_type == null)
			throw new ServerException(Status.NOT_ACCEPTABLE,
					"The browser_type is missing");
		if (crawlDefinition.entry_url == null)
			throw new ServerException(Status.NOT_ACCEPTABLE,
					"The entry_url is missing");
		parametersMatcherList = getRegExpMatcherList(crawlDefinition.parameters_patterns);
		exclusionMatcherList = getWildcardMatcherList(crawlDefinition.exclusion_patterns);
		inclusionMatcherList = getWildcardMatcherList(crawlDefinition.inclusion_patterns);
		try {
			internetDomainName = InternetDomainName.from(new URI(crawlDefinition.entry_url).getHost());
		} catch (URISyntaxException e) {
			throw new ServerException(Status.NOT_ACCEPTABLE, e.getMessage());
		}
	}

	private final static List<Matcher> getRegExpMatcherList(List<String> patternList)
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

	private final static List<WildcardMatcher> getWildcardMatcherList(List<String> patternList) {
		if (patternList == null || patternList.isEmpty())
			return null;
		List<WildcardMatcher> matcherList = new ArrayList<WildcardMatcher>(
				patternList.size());
		for (String pattern : patternList)
			matcherList.add(new WildcardMatcher(pattern));
		return matcherList;
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

	private final static boolean checkRegExpMatcher(String value,
													List<Matcher> matcherList) {
		for (Matcher matcher : matcherList) {
			matcher.reset(value);
			if (matcher.find())
				return true;
		}
		return false;
	}

	private final static boolean checkWildcardMatcher(String value,
													  List<WildcardMatcher> matcherList) {
		for (WildcardMatcher matcher : matcherList)
			if (matcher.match(value))
				return true;
		return false;
	}

	/**
	 * Check the inclusion list. Returns null if the inclusion list is empty.
	 *
	 * @param uriString
	 * @return
	 */
	private Boolean matchesInclusion(String uriString) {
		if (inclusionMatcherList == null || inclusionMatcherList.isEmpty())
			return null;
		return checkWildcardMatcher(uriString, inclusionMatcherList);
	}

	/**
	 * Check the exclusion list. Returns null if the exclusion list is empty.
	 *
	 * @param uriString
	 * @return
	 */
	private Boolean matchesExclusion(String uriString) {
		if (exclusionMatcherList == null || exclusionMatcherList.isEmpty())
			return false;
		return checkWildcardMatcher(uriString, exclusionMatcherList);
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
			if (!checkRegExpMatcher(param.getName() + "=" + param.getValue(),
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

	private Collection<URI> checkLinks(Collection<URI> uris) {
		if (uris == null) return null;
		Map<String, URI> linkMap = new LinkedHashMap<String, URI>();
		for (URI linkURI : uris) {
			linkURI = checkLink(linkURI);
			if (linkURI != null)
				linkMap.put(linkURI.toString(), linkURI);
		}
		return linkMap.values();
	}

	private boolean matchesInitialDomain(URI uri) {
		String host = uri.getHost();
		if (StringUtils.isEmpty(host))
			return false;
		if (!InternetDomainName.isValid(host))
			return false;
		return internetDomainName.equals(InternetDomainName.from(host));
	}

	private String scriptBeforeCrawl(CurrentURI currentURI, String uriString) throws ServerException {
		URI uri = currentURI.getURI();
		if (uriString == null)
			uriString = uri.toString();


		currentURI.setStartDomain(matchesInitialDomain(uri));
		//currentURI.setStartSubDomain(matchesInitialSubDomain(uri));

		//We check the inclusion/exclusion.
		currentURI.setInInclusion(matchesInclusion(uriString));
		currentURI.setInExclusion(matchesExclusion(uriString));

		if (currentURI.isInInclusion() != null && !currentURI.isInInclusion())
			currentURI.setIgnored(true);

		if (currentURI.isInExclusion() != null && currentURI.isInExclusion())
			currentURI.setIgnored(true);

		script(EventEnum.before_crawl, currentURI);
		return uriString;
	}

	private void crawl(CurrentURI currentURI) {

		if (session.isAborting())
			return;

		URI uri = currentURI.getInitialURI();
		String uriString = uri.toString();
		session.setCurrentURI(uriString);

		// Check if the URL is well formated
		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme)
				&& !"https".equalsIgnoreCase(scheme)) {
			session.incIgnoredCount();
			currentURI.setIgnored(true);
			if (logger.isInfoEnabled())
				logger.info("Ignored (not http) " + uri);
			return;
		}

		// Load the URL
		if (logger.isInfoEnabled())
			logger.info("Crawling " + uri + " (" + currentURI.getDepth() + ")");
		try {
			driver.get(uriString);
		} catch (WebDriverException e) {
			session.incErrorCount();
			currentURI.setError(e);
			return;
		}

		try {
			uriString = driver.getCurrentUrl();
			uri = new URI(uriString);
			currentURI.setFinalURI(uri);
		} catch (URISyntaxException e) {
			session.incErrorCount();
			currentURI.setError(e);
			return;
		}

		// Check again with exclusion/inclusion list
		// in case of redirection
		if (currentURI.isRedirected()) {
			if (logger.isInfoEnabled())
				logger.info("Redirected " + currentURI.getInitialURI() + " to " + uriString);
			try {
				scriptBeforeCrawl(currentURI, uriString);
			} catch (Exception e) {
				session.incErrorCount();
				currentURI.setError(e);
				return;
			}
			if (currentURI.isIgnored()) {
				session.incIgnoredCount();
				return;
			}
		}

		session.incCrawledCount();
		currentURI.setCrawled();

		// Let's look for the a tags
		Set<String> hrefSet = new LinkedHashSet<String>();
		driver.findLinks(driver, hrefSet);
		if (hrefSet.isEmpty())
			return;
		ArrayList<URI> uris = new ArrayList<URI>(hrefSet.size());
		currentURI.hrefToURICollection(hrefSet, uris);
		currentURI.setLinks(uris);
		if (logger.isInfoEnabled())
			logger.info("Link founds " + uri + " : " + uris.size());
	}

	private void crawlOne(final Set<URI> crawledURIs, URI uri, final Set<URI> nextLevelURIs, final int depth)
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

		// Give the hand to the "before_crawl" script
		scriptBeforeCrawl(currentURI, null);

		if (!currentURI.isIgnored()) {
			crawl(currentURI);
			// Store the final URI (in case of redirection)
			if (crawledURIs != null)
				crawledURIs.add(currentURI.getURI());
		}
		script(EventEnum.after_crawl, currentURI);


		Collection<URI> newLinks = checkLinks(currentURI.getLinks());
		currentURI.setLinks(newLinks);
		if (newLinks != null)
			nextLevelURIs.addAll(newLinks);

	}

	private void crawlLevel(Set<URI> crawledURIs, Collection<URI> levelURIs, int depth)
			throws ServerException {

		if (session.isAborting())
			return;

		if (levelURIs == null || levelURIs.isEmpty())
			return;

		final Set<URI> nextLevelURIs = new HashSet<URI>();

		// Crawl all URLs from the level
		for (URI uri : levelURIs)
			crawlOne(crawledURIs, uri, nextLevelURIs, depth);

		// Check if we reach the max depth
		depth++;
		if (crawlDefinition.max_depth == null
				|| depth > crawlDefinition.max_depth)
			return;

		// Let's crawl the next level if any
		crawlLevel(crawledURIs, nextLevelURIs, depth);

	}

	/**
	 * Execute the script related to the passed event.
	 *
	 * @param event      the expected event
	 * @param currentURI the current URI description
	 * @return true if the script was executed, false if no script is attached to the event
	 * @throws ServerException if the execution of the script failed
	 */
	private boolean script(EventEnum event, CurrentURI currentURI)
			throws ServerException {
		if (crawlDefinition.scripts == null)
			return false;
		Script script = crawlDefinition.scripts.get(event);
		if (script == null)
			return false;
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
		return true;
	}

	private void runner() throws URISyntaxException,
			IOException, ScriptException, ServerException, ReflectiveOperationException {
		try {
			driver = new BrowserDriverBuilder(crawlDefinition).build();
			script(EventEnum.before_session, null);
			final Set<URI> crawledURIs = new HashSet<URI>();
			crawlLevel(crawledURIs, Arrays.asList(new URI(crawlDefinition.entry_url)), 0);
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
