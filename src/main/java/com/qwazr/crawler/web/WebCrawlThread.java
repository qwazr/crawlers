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
package com.qwazr.crawler.web;

import com.google.common.net.InternetDomainName;
import com.qwazr.crawler.common.CrawlSessionImpl;
import com.qwazr.crawler.common.CrawlThread;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.web.driver.BrowserDriver;
import com.qwazr.crawler.web.driver.BrowserDriverBuilder;
import com.qwazr.crawler.web.robotstxt.RobotsTxt;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RegExpUtils;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.UBuilder;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import javax.script.ScriptException;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public class WebCrawlThread extends CrawlThread<WebCrawlerManager> {

	private static final Logger LOGGER = LoggerUtils.getLogger(WebCrawlThread.class);

	private final WebCrawlDefinition crawlDefinition;
	private final InternetDomainName internetDomainName;

	private final List<Matcher> parametersMatcherList;
	private final List<Matcher> pathCleanerMatcherList;

	private BrowserDriver driver = null;

	private final Map<URI, RobotsTxt> robotsTxtMap;
	private final String robotsTxtUserAgent;

	private final TimeTracker timeTracker;

	WebCrawlThread(final WebCrawlerManager webCrawlerManager, final String sessionName,
			final WebCrawlDefinition crawlDefinition) throws ServerException {
		super(webCrawlerManager, new CrawlSessionImpl<>(sessionName, webCrawlerManager.getMyAddress(), crawlDefinition,
				crawlDefinition.entryUrl), LOGGER);
		this.crawlDefinition = crawlDefinition;
		this.timeTracker = session.getTimeTracker();
		if (crawlDefinition.browserType == null)
			throw new ServerException(Status.NOT_ACCEPTABLE, "The browser_type is missing");
		if (crawlDefinition.entryUrl == null && crawlDefinition.entryRequest == null)
			throw new ServerException(Status.NOT_ACCEPTABLE, "Either the entry_url or the entry_request is missing");
		try {
			parametersMatcherList = RegExpUtils.getMatcherList(crawlDefinition.parametersPatterns);
			pathCleanerMatcherList = RegExpUtils.getMatcherList(crawlDefinition.pathCleanerPatterns);
		} catch (PatternSyntaxException e) {
			throw new ServerException(Status.NOT_ACCEPTABLE, e.getMessage());
		}
		if (crawlDefinition.robotsTxtEnabled != null && crawlDefinition.robotsTxtEnabled)
			robotsTxtMap = new HashMap<>();
		else
			robotsTxtMap = null;
		robotsTxtUserAgent =
				crawlDefinition.robotsTxtUseragent == null ? "QWAZR_BOT" : crawlDefinition.robotsTxtUseragent;
		final String u;
		try {
			u = crawlDefinition.entryUrl != null ? crawlDefinition.entryUrl : crawlDefinition.entryRequest.url;
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

	/**
	 * Remove the fragment and the query parameters following the configuration
	 *
	 * @param uri
	 * @return
	 */
	private URI checkLink(final URI uri) {
		UBuilder uriBuilder = new UBuilder(uri);
		if (crawlDefinition.removeFragments != null && crawlDefinition.removeFragments)
			uriBuilder.setFragment(null);
		if (parametersMatcherList != null && !parametersMatcherList.isEmpty())
			uriBuilder.removeMatchingParameters(parametersMatcherList);
		if (pathCleanerMatcherList != null && !pathCleanerMatcherList.isEmpty())
			uriBuilder.cleanPath(pathCleanerMatcherList);
		try {
			return uriBuilder.build();
		} catch (URISyntaxException e) {
			LOGGER.log(Level.WARNING, e, () -> "Cannot build the URI from " + uri.toString());
			return null;
		}
	}

	private Collection<URI> checkLinks(final Collection<URI> uris) {
		if (uris == null)
			return null;
		Map<String, URI> linkMap = new LinkedHashMap<>();
		for (URI linkURI : uris) {
			linkURI = checkLink(linkURI);
			if (linkURI != null)
				linkMap.put(linkURI.toString(), linkURI);
		}
		return linkMap.values();
	}

	private boolean matchesInitialDomain(final URI uri) {
		final String host = uri.getHost();
		return !StringUtils.isEmpty(host) && InternetDomainName.isValid(host) &&
				internetDomainName.equals(InternetDomainName.from(host));
	}

	private void scriptBeforeCrawl(final CurrentURIImpl currentURI, String uriString) {
		final URI uri = currentURI.getURI();
		if (uriString == null)
			uriString = uri.toString();

		currentURI.setStartDomain(matchesInitialDomain(uri));

		checkPassInclusionExclusion(currentURI, uriString);
		script(EventEnum.before_crawl, currentURI);
	}

	private void crawl(final CurrentURIImpl currentURI, final CrawlProvider crawlProvider) {

		if (session.isAborting())
			return;

		URI uri = currentURI.getInitialURI();
		String uriString = uri.toString();
		session.setCurrentCrawl(uriString, currentURI.getDepth());

		// Check if the URL is well formated
		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			session.incIgnoredCount();
			currentURI.setIgnored();
			LOGGER.info(() -> "Ignored (not http) " + currentURI.getInitialURI());
			return;
		}

		// Set the optional cookies
		if (crawlDefinition.cookies != null) {
			final WebDriver.Options options = driver.manage();
			crawlDefinition.cookies.forEach((name, value) -> {
				if (options.getCookieNamed(name) != null)
					options.deleteCookieNamed(name);
				options.addCookie(new Cookie(name, value));
			});
		}

		// Load the URL
		LOGGER.info(() -> "Crawling " + currentURI.getInitialURI() + " (" + currentURI.getDepth() + ")");
		try {
			timeTracker.next(null);
			crawlProvider.apply();
			//if (mainWindow != null && !mainWindow.equals(driver.getWindowHandle()))
			//	driver.switchTo().window(mainWindow);
		} catch (Exception e) {
			session.incErrorCount("Error on " + uriString + ": " + e.getMessage());
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
			session.incErrorCount("Error on " + uriString + ": " + e.getMessage());
			currentURI.setError(driver, e);
			return;
		}

		// Check again with exclusion/inclusion list
		// in case of redirection
		if (currentURI.isRedirected()) {
			LOGGER.info(() -> "Redirected " + currentURI.getInitialURI() + " to " + currentURI.getUri());
			try {
				scriptBeforeCrawl(currentURI, uriString);
			} catch (Exception e) {
				session.incErrorCount("Error on " + uriString + ": " + e.getMessage());
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
						LOGGER.warning(() -> "Invalid URI in base HREF: " + href + " in " + currentURI.getUri());
					}
				}
			} catch (org.openqa.selenium.NoSuchElementException e) {
				// OK that's not an error
			} catch (Exception e) {
				LOGGER.warning(() -> "Cannot locate base href for " + currentURI.getUri() + " " + e.getMessage());
			}
		}

		final int crawledCount = session.incCrawledCount();
		currentURI.setCrawled();
		if (crawlDefinition.maxUrlNumber != null && crawledCount >= crawlDefinition.maxUrlNumber)
			abort("Max URL number reached: " + crawlDefinition.maxUrlNumber);

		// Let's look for the a tags
		final Set<String> hrefSet = new LinkedHashSet<>();
		try {
			timeTracker.next(null);
			driver.findLinks(driver, hrefSet);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e, () -> "Cannot extract links from " + currentURI.getUri());
		} finally {
			timeTracker.next("Find links");
		}
		if (hrefSet.isEmpty())
			return;
		final ArrayList<URI> uris = new ArrayList<>(hrefSet.size());
		currentURI.hrefToURICollection(hrefSet, uris);
		currentURI.setLinks(uris);
		LOGGER.info(() -> "Link founds " + currentURI.getUri() + " : " + uris.size());

		final ArrayList<URI> filteredURIs = new ArrayList<>();
		for (URI u : uris) {
			u = checkLink(u);
			if (u == null)
				continue;
			final String us = u.toString();
			if (checkPassInclusionExclusion(us, null, null))
				filteredURIs.add(u);
		}
		currentURI.setFilteredLinks(filteredURIs);
	}

	private RobotsTxt.Status checkRobotsTxt(CurrentURI currentURI)
			throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException {
		if (robotsTxtMap == null)
			return null;
		timeTracker.next(null);
		try {
			final URI uri = currentURI.getURI();
			final URI robotsTxtURI = RobotsTxt.getRobotsURI(uri);
			RobotsTxt robotsTxt = robotsTxtMap.get(robotsTxtURI);
			if (robotsTxt == null) {
				robotsTxt = RobotsTxt.download(driver.getProxy(), robotsTxtUserAgent, robotsTxtURI);
				robotsTxtMap.put(robotsTxtURI, robotsTxt);
			}
			return robotsTxt.getStatus(uri, robotsTxtUserAgent);
		} finally {
			timeTracker.next("Robots.txt check");
		}
	}

	private void crawlOne(final Set<URI> crawledURIs, final CrawlProvider crawlProvider, final Set<URI> nextLevelURIs,
			final int depth) throws IOException, InterruptedException {

		if (session.isAborting())
			return;

		// Check if it has been already crawled
		if (crawledURIs != null) {
			if (crawledURIs.contains(crawlProvider.uri))
				return;
			crawledURIs.add(crawlProvider.uri);
		}

		final CurrentURIImpl currentURI = new CurrentURIImpl(crawlProvider.uri, depth);

		// Give the hand to the "before_crawl" scripts
		scriptBeforeCrawl(currentURI, null);

		if (!currentURI.isIgnored()) {

			if (crawlDefinition.crawlWaitMs != null)
				Thread.sleep(crawlDefinition.crawlWaitMs);

			// Check the robotsTxt status
			try {
				final RobotsTxt.Status robotsTxtStatus = checkRobotsTxt(currentURI);
				if (robotsTxtStatus != null && !robotsTxtStatus.isCrawlable) {
					currentURI.setIgnored();
					currentURI.setRobotsTxtDisallow(true);
				}
			} catch (Exception e) {
				session.incErrorCount("Error on robots.txt extraction: " + e.getMessage());
				currentURI.setError(driver, e);
			}

			if (!currentURI.isIgnored() && currentURI.getError() == null) {
				crawl(currentURI, crawlProvider);

				// Store the final URI (in case of redirection)
				if (crawledURIs != null)
					crawledURIs.add(currentURI.getURI());
			}
		}

		// Give the hand to the "after_crawl" scripts
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

		if (crawlDefinition.maxDepth == null || depth > crawlDefinition.maxDepth)
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
				crawlOne(crawledURIs, new GetProvider(uri), null, depth == null ? 0 : depth);
			} catch (URISyntaxException e) {
				LOGGER.warning(() -> "Malformed URI: " + uri);
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, e, () -> "Interruption on " + uri);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, e, () -> "IO Exception on " + uri);
			}
		});
	}

	protected void runner()
			throws URISyntaxException, IOException, ScriptException, ServerException, ReflectiveOperationException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
		try {
			driver = new BrowserDriverBuilder(crawlDefinition).build();
			registerScriptGlobalObject("driver", driver);
			script(EventEnum.before_session, null);
			if (crawlDefinition.preUrl != null && !crawlDefinition.preUrl.isEmpty())
				driver.get(crawlDefinition.preUrl);
			final Set<URI> crawledURIs = new HashSet<>();
			if (crawlDefinition.urls != null && !crawlDefinition.urls.isEmpty())
				crawlUrlMap(crawledURIs, crawlDefinition.urls);
			else {
				if (crawlDefinition.entryUrl != null)
					crawlStart(crawledURIs, new GetProvider(crawlDefinition.entryUrl));
				else if (crawlDefinition.entryRequest != null)
					crawlStart(crawledURIs, new RequestProvider(crawlDefinition.entryRequest));
			}
		} finally {
			try {
				if (driver != null)
					driver.quit();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, e, e::getMessage);
			}
			script(EventEnum.after_session, null);
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
