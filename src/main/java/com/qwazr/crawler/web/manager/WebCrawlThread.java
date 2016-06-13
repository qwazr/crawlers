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

import com.google.common.net.InternetDomainName;
import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.crawler.web.CurrentURI;
import com.qwazr.crawler.web.driver.BrowserDriver;
import com.qwazr.crawler.web.driver.BrowserDriverBuilder;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.crawler.web.service.WebCrawlDefinition.EventEnum;
import com.qwazr.crawler.web.service.WebCrawlDefinition.Script;
import com.qwazr.crawler.web.service.WebCrawlStatus;
import com.qwazr.crawler.web.service.WebRequestDefinition;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.scripts.ScriptRunThread;
import com.qwazr.utils.RegExpUtils;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.UBuilder;
import com.qwazr.utils.WildcardMatcher;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public class WebCrawlThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(WebCrawlThread.class);

	private final CurrentSessionImpl session;
	final WebCrawlDefinition crawlDefinition;
	private final InternetDomainName internetDomainName;

	private final List<Matcher> parametersMatcherList;
	private final List<Matcher> pathCleanerMatcherList;
	private final List<WildcardMatcher> exclusionMatcherList;
	private final List<WildcardMatcher> inclusionMatcherList;

	private BrowserDriver driver = null;

	private final Map<URI, RobotsTxt> robotsTxtMap;
	private final String robotsTxtUserAgent;

	private final TimeTracker timeTracker;

	WebCrawlThread(final String sessionName, final WebCrawlDefinition crawlDefinition) throws ServerException {
		timeTracker = new TimeTracker();
		this.session = new CurrentSessionImpl(crawlDefinition, sessionName, timeTracker);
		this.crawlDefinition = crawlDefinition;
		if (crawlDefinition.browser_type == null)
			throw new ServerException(Status.NOT_ACCEPTABLE, "The browser_type is missing");
		if (crawlDefinition.entry_url == null && crawlDefinition.entry_request == null)
			throw new ServerException(Status.NOT_ACCEPTABLE, "Either the entry_url or the entry_request is missing");
		try {
			parametersMatcherList = RegExpUtils.getMatcherList(crawlDefinition.parameters_patterns);
			pathCleanerMatcherList = RegExpUtils.getMatcherList(crawlDefinition.path_cleaner_patterns);
		} catch (PatternSyntaxException e) {
			throw new ServerException(Status.NOT_ACCEPTABLE, e.getMessage());
		}
		exclusionMatcherList = WildcardMatcher.getList(crawlDefinition.exclusion_patterns);
		inclusionMatcherList = WildcardMatcher.getList(crawlDefinition.inclusion_patterns);
		if (crawlDefinition.robots_txt_enabled != null && crawlDefinition.robots_txt_enabled)
			robotsTxtMap = new HashMap<>();
		else
			robotsTxtMap = null;
		robotsTxtUserAgent =
				crawlDefinition.robots_txt_useragent == null ? "QWAZR_BOT" : crawlDefinition.robots_txt_useragent;
		final String u;
		try {
			u = crawlDefinition.entry_url != null ? crawlDefinition.entry_url : crawlDefinition.entry_request.url;
			URI uri = new URI(u);
			String host = uri.getHost();
			if (host == null)
				throw new URISyntaxException(u, "No host found.", -1);
			internetDomainName = InternetDomainName.from(host);
		} catch (URISyntaxException e) {
			throw new ServerException(Status.NOT_ACCEPTABLE, e.getMessage());
		} finally {
			timeTracker.next("Initialization");
		}
	}

	String getSessionName() {
		return session.getName();
	}

	WebCrawlStatus getStatus() {
		return new WebCrawlStatus(ClusterManager.INSTANCE.me.httpAddressKey, crawlDefinition.entry_url, session);
	}

	void abort(String reason) {
		session.abort(reason);
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
		return WildcardMatcher.anyMatch(uriString, inclusionMatcherList);
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
		return WildcardMatcher.anyMatch(uriString, exclusionMatcherList);
	}

	/**
	 * Remove the fragment and the query parameters following the configuration
	 *
	 * @param uri
	 * @return
	 */
	private URI checkLink(URI uri) {
		UBuilder uriBuilder = new UBuilder(uri);
		if (crawlDefinition.remove_fragments != null && crawlDefinition.remove_fragments)
			uriBuilder.setFragment(null);
		if (parametersMatcherList != null && !parametersMatcherList.isEmpty())
			uriBuilder.removeMatchingParameters(parametersMatcherList);
		if (pathCleanerMatcherList != null && !pathCleanerMatcherList.isEmpty())
			uriBuilder.cleanPath(pathCleanerMatcherList);
		try {
			return uriBuilder.build();
		} catch (URISyntaxException e) {
			logger.warn("Cannot build the URI from " + uri.toString(), e);
			return null;
		}
	}

	private Collection<URI> checkLinks(Collection<URI> uris) {
		if (uris == null)
			return null;
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

	private String scriptBeforeCrawl(CurrentURIImpl currentURI, String uriString)
			throws ServerException, IOException, ClassNotFoundException {
		URI uri = currentURI.getURI();
		if (uriString == null)
			uriString = uri.toString();

		currentURI.setStartDomain(matchesInitialDomain(uri));

		// We check the inclusion/exclusion.
		currentURI.setInInclusion(matchesInclusion(uriString));
		currentURI.setInExclusion(matchesExclusion(uriString));

		if (currentURI.isInInclusion() != null && !currentURI.isInInclusion())
			currentURI.setIgnored(true);

		if (currentURI.isInExclusion() != null && currentURI.isInExclusion())
			currentURI.setIgnored(true);

		script(EventEnum.before_crawl, currentURI);
		return uriString;
	}

	private void crawl(final CurrentURIImpl currentURI, final CrawlProvider crawlProvider) {

		if (session.isAborting())
			return;

		URI uri = currentURI.getInitialURI();
		String uriString = uri.toString();
		session.setCurrentURI(uriString, currentURI.getDepth());

		// Check if the URL is well formated
		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
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
			timeTracker.next(null);
			crawlProvider.apply();
			//if (mainWindow != null && !mainWindow.equals(driver.getWindowHandle()))
			//	driver.switchTo().window(mainWindow);
		} catch (Exception e) {
			session.incErrorCount();
			currentURI.setError(driver, e);
			return;
		} finally {
			timeTracker.next("Driver.getURL");
		}

		try {
			uriString = driver.getCurrentUrl();
			uri = new URI(uriString);
			currentURI.setFinalURI(uri);
		} catch (URISyntaxException e) {
			session.incErrorCount();
			currentURI.setError(driver, e);
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
				currentURI.setError(driver, e);
				return;
			}
			if (currentURI.isIgnored()) {
				session.incIgnoredCount();
				return;
			}
		}

		// Support of the base/href element
		boolean searchBaseHref = true;
		try {
			searchBaseHref = "text/html".equals(driver.getContentType());
		} catch (WebDriverException e) {
			// OK that's not really an error
		}

		if (searchBaseHref) {
			try {
				WebElement baseElement = driver.findElement(By.tagName("base"));
				if (baseElement != null) {
					String href = baseElement.getAttribute("href");
					try {
						currentURI.setBaseURI(new URI(href));
					} catch (URISyntaxException e) {
						if (logger.isWarnEnabled())
							logger.warn("Invalid URI in base HREF: " + href + " in " + uriString);
					}
				}
			} catch (org.openqa.selenium.NoSuchElementException e) {
				// OK that's not really an error
			} catch (Exception e) {
				if (logger.isWarnEnabled())
					logger.warn("Cannot locate base href for " + uriString + " " + e.getMessage());
			}
		}

		final int crawledCount = session.incCrawledCount();
		currentURI.setCrawled();
		if (crawlDefinition.max_url_number != null && crawledCount >= crawlDefinition.max_url_number)
			abort("Max URL number reached: " + crawlDefinition.max_url_number);

		// Let's look for the a tags
		final Set<String> hrefSet = new LinkedHashSet<>();
		try {
			timeTracker.next(null);
			driver.findLinks(driver, hrefSet);
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn("Cannot extract links from " + uriString, e);
		} finally {
			timeTracker.next("Find links");
		}
		if (hrefSet.isEmpty())
			return;
		final ArrayList<URI> uris = new ArrayList<>(hrefSet.size());
		currentURI.hrefToURICollection(hrefSet, uris);
		currentURI.setLinks(uris);
		if (logger.isInfoEnabled())
			logger.info("Link founds " + uri + " : " + uris.size());

		final ArrayList<URI> filteredURIs = new ArrayList<>();
		for (URI u : uris) {
			String us = u.toString();
			Boolean inc = matchesInclusion(us);
			if (inc != null && !inc)
				continue;
			Boolean exc = matchesExclusion(us);
			if (exc != null && exc)
				continue;
			filteredURIs.add(u);
		}
		currentURI.setFilteredLinks(filteredURIs);
	}

	private RobotsTxt.RobotsTxtStatus checkRobotsTxt(CurrentURI currentURI)
			throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException {
		if (robotsTxtMap == null)
			return null;
		timeTracker.next(null);
		try {
			URI uri = currentURI.getURI();
			URI robotsTxtURI = RobotsTxt.getRobotsURI(uri);
			RobotsTxt robotsTxt = robotsTxtMap.get(robotsTxtURI);
			if (robotsTxt == null) {
				robotsTxt = RobotsTxt.download(driver.getProxy(), robotsTxtUserAgent, robotsTxtURI);
				robotsTxtMap.put(robotsTxtURI, robotsTxt);
			}
			return robotsTxt.getStatus(uri);
		} finally {
			timeTracker.next("Robots.txt check");
		}
	}

	private void crawlOne(final Set<URI> crawledURIs, final CrawlProvider crawlProvider, final Set<URI> nextLevelURIs,
			final int depth) throws ServerException, IOException, ClassNotFoundException, InterruptedException {

		if (session.isAborting())
			return;

		// Check if it has been already crawled
		if (crawledURIs != null) {
			if (crawledURIs.contains(crawlProvider.uri))
				return;
			crawledURIs.add(crawlProvider.uri);
		}

		CurrentURIImpl currentURI = new CurrentURIImpl(crawlProvider.uri, depth);

		// Give the hand to the "before_crawl" scripts
		scriptBeforeCrawl(currentURI, null);

		if (!currentURI.isIgnored()) {

			if (crawlDefinition.crawl_wait_ms != null)
				Thread.sleep(crawlDefinition.crawl_wait_ms);

			// Check the robotsTxt status
			try {
				RobotsTxt.RobotsTxtStatus robotsTxtStatus = checkRobotsTxt(currentURI);
				if (robotsTxtStatus != null && !robotsTxtStatus.isCrawlable) {
					currentURI.setIgnored(true);
					currentURI.setRobotsTxtDisallow(true);
				}
			} catch (Exception e) {
				session.incErrorCount();
				currentURI.setError(driver, e);
			}

			if (!currentURI.isIgnored() && currentURI.getError() == null) {
				crawl(currentURI, crawlProvider);

				// Store the final URI (in case of redirection)
				if (crawledURIs != null)
					crawledURIs.add(currentURI.getURI());
			}
		}
		script(EventEnum.after_crawl, currentURI);

		Collection<URI> sameLevelLinks = checkLinks(currentURI.getSameLevelLinks());
		if (sameLevelLinks != null)
			for (URI sameLevelURI : sameLevelLinks)
				crawlOne(crawledURIs, new GetProvider(sameLevelURI), nextLevelURIs, depth);

		Collection<URI> newLinks = checkLinks(currentURI.getLinks());
		currentURI.setLinks(newLinks);
		if (newLinks != null && nextLevelURIs != null)
			nextLevelURIs.addAll(newLinks);

	}

	private void crawlSubLevel(final Set<URI> crawledURIs, final Collection<URI> levelURIs, final int depth)
			throws ServerException, IOException, URISyntaxException, NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException, ClassNotFoundException, InterruptedException {

		if (crawlDefinition.max_depth == null || depth > crawlDefinition.max_depth)
			return;

		if (session.isAborting())
			return;

		if (levelURIs == null || levelURIs.isEmpty())
			return;

		final Set<URI> nextLevelURIs = new HashSet<>();

		// Crawl all URLs from the level
		for (URI uri : levelURIs)
			crawlOne(crawledURIs, new GetProvider(uri), nextLevelURIs, depth);

		// Let's crawl the next level if any
		crawlSubLevel(crawledURIs, nextLevelURIs, depth + 1);
	}

	private void crawlStart(final Set<URI> crawledURIs, final CrawlProvider crawlProvider)
			throws NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, URISyntaxException,
			ClassNotFoundException, InterruptedException {
		final Set<URI> nextLevelURIs = new HashSet<>();
		crawlOne(crawledURIs, crawlProvider, nextLevelURIs, 0);
		crawlSubLevel(crawledURIs, nextLevelURIs, 1);
	}

	private void crawlUrlMap(Set<URI> crawledURIs, Map<String, Integer> urlMap) {
		urlMap.forEach((uri, depth) -> {
			try {
				crawlOne(crawledURIs, new GetProvider(uri), null, depth);
			} catch (Exception e) {
				logger.warn("Malformed URI: " + uri);
			}
		});
	}

	/**
	 * Execute the scripts related to the passed event.
	 *
	 * @param event      the expected event
	 * @param currentURI the current URI description
	 * @return true if the scripts was executed, false if no scripts is attached
	 * to the event
	 * @throws ServerException        if the execution of the scripts failed
	 * @throws IOException            if any I/O exception occurs
	 * @throws ClassNotFoundException if the JAVA class is not found
	 */
	private boolean script(EventEnum event, CurrentURI currentURI)
			throws ServerException, IOException, ClassNotFoundException {
		if (crawlDefinition.scripts == null)
			return false;
		Script script = crawlDefinition.scripts.get(event);
		if (script == null)
			return false;
		timeTracker.next(null);
		try {
			Map<String, Object> objects = new TreeMap<String, Object>();
			objects.put("session", session);
			if (script.variables != null)
				objects.putAll(script.variables);
			if (driver != null)
				objects.put("driver", driver);
			if (currentURI != null)
				objects.put("current", currentURI);
			ScriptRunThread scriptRunThread = ScriptManager.getInstance().runSync(script.name, objects);
			if (scriptRunThread.getException() != null)
				throw new ServerException(scriptRunThread.getException());
			return true;
		} finally {
			timeTracker.next("Event: " + event.name());
		}
	}

	private void runner()
			throws URISyntaxException, IOException, ScriptException, ServerException, ReflectiveOperationException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
		try {
			driver = new BrowserDriverBuilder(crawlDefinition).build();
			script(EventEnum.before_session, null);
			final Set<URI> crawledURIs = new HashSet<>();
			if (crawlDefinition.urls != null && !crawlDefinition.urls.isEmpty())
				crawlUrlMap(crawledURIs, crawlDefinition.urls);
			else {
				if (crawlDefinition.entry_url != null)
					crawlStart(crawledURIs, new GetProvider(crawlDefinition.entry_url));
				else if (crawlDefinition.entry_request != null)
					crawlStart(crawledURIs, new RequestProvider(crawlDefinition.entry_request));
			}
		} finally {
			try {
				if (driver != null)
					driver.quit();
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
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

	private abstract class CrawlProvider {

		protected final URI uri;

		protected CrawlProvider(String uriString) throws URISyntaxException {
			this.uri = new URI(uriString);
		}

		protected CrawlProvider(URI uri) {
			this.uri = uri;
		}

		protected abstract void apply();
	}

	private class GetProvider extends CrawlProvider {

		private GetProvider(URI uri) {
			super(uri);
		}

		private GetProvider(String uriString) throws URISyntaxException {
			super(uriString);
		}

		@Override
		protected void apply() {
			driver.get(uri.toString());
		}
	}

	private class RequestProvider extends CrawlProvider {

		private final WebRequestDefinition request;

		private RequestProvider(WebRequestDefinition request) throws URISyntaxException {
			super(request.url);
			this.request = request;
		}

		@Override
		protected void apply() {
			driver.request(request);
		}
	}
}
