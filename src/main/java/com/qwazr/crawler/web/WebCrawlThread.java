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
import com.qwazr.crawler.web.driver.DriverInterface;
import com.qwazr.crawler.web.robotstxt.RobotsTxt;
import com.qwazr.server.ServerException;
import com.qwazr.utils.FunctionUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RegExpUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.script.ScriptException;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public class WebCrawlThread extends CrawlThread<WebCrawlDefinition, WebCrawlStatus, WebCrawlerManager> {

	private static final Logger LOGGER = LoggerUtils.getLogger(WebCrawlThread.class);

	private final WebCrawlDefinition crawlDefinition;
	private final InternetDomainName internetDomainName;

	private final List<Matcher> parametersMatcherList;
	private final List<Matcher> pathCleanerMatcherList;

	private final Map<URI, RobotsTxt> robotsTxtMap;
	private final String userAgent;

	private final TimeTracker timeTracker;

	WebCrawlThread(final WebCrawlerManager webCrawlerManager,
			CrawlSessionImpl<WebCrawlDefinition, WebCrawlStatus> session, final WebCrawlDefinition crawlDefinition)
			throws ServerException {
		super(webCrawlerManager, session, LOGGER);
		this.crawlDefinition = crawlDefinition;
		this.timeTracker = session.getTimeTracker();
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
		userAgent = crawlDefinition.userAgent == null ? "QWAZR_BOT" : crawlDefinition.userAgent;
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
	 * @param uri the URI to check
	 * @return the transformed URI
	 */
	private URI transformLink(final URI uri) {
		final UBuilder uriBuilder = new UBuilder(uri);
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

	private <T extends DriverInterface.Head> T doHttp(final String uriString,
			final CurrentURIImpl.Builder currentBuilder,
			final FunctionUtils.FunctionEx<String, T, IOException> method) {
		try {
			timeTracker.next(null);
			final T head = method.apply(uriString);
			final String redirectLocation = head.getRedirectLocation();
			if (!StringUtils.isBlank(redirectLocation)) {
				final URI redirectUri = new URI(redirectLocation);
				currentBuilder.redirect(redirectUri);
				currentBuilder.link(redirectUri);
				return head;
			}
			return head;
		} catch (Exception e) {
			session.incErrorCount("Error on " + uriString + ": " + e.getMessage());
			currentBuilder.error(e);
			return null;
		} finally {
			timeTracker.next("HTTP");
		}
	}

	/**
	 * @param currentBuilder
	 */

	private void crawl(final DriverInterface driver, final CurrentURIImpl.Builder currentBuilder)
			throws InterruptedException {

		if (session.isAborting())
			return;

		final String uriString = currentBuilder.uri.toString();

		session.setCurrentCrawl(uriString, currentBuilder.depth);

		// Check the SCHEME, we only accept http or https
		final String scheme = currentBuilder.uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			session.incIgnoredCount();
			currentBuilder.ignored(true);
			LOGGER.info(() -> "Ignored (not http) " + uriString);
			return;
		}

		// Check the inclusion/exclusion rules
		if (!checkPassInclusionExclusion(uriString, currentBuilder::inInclusion, currentBuilder::inExclusion))
			return;

		if (crawlDefinition.crawlWaitMs != null)
			Thread.sleep(crawlDefinition.crawlWaitMs);

		// Check the robotsTxt status
		try {
			final RobotsTxt.Status robotsTxtStatus = checkRobotsTxt(driver, currentBuilder);
			if (robotsTxtStatus != null && !robotsTxtStatus.isCrawlable) {
				currentBuilder.ignored(true);
				currentBuilder.robotsTxtDisallow(true);
			}
		} catch (Exception e) {
			session.incErrorCount("Error during robots.txt extraction: " + e.getMessage());
			currentBuilder.error(e);
			return;
		}

		// First make an head
		final DriverInterface.Head head = doHttp(uriString, currentBuilder, (u) -> driver.head(u));
		if (head == null)
			return; // Any error already handled by doHttp

		//TODO: check content-type, content-length

		// Second make an head
		final DriverInterface.Get get = doHttp(uriString, currentBuilder, (u) -> driver.get(u));
		if (get == null)
			return; // Any error already handled by doHttp

		// Handle url number limit
		final int crawledCount = session.incCrawledCount();
		currentBuilder.crawled(true);
		if (crawlDefinition.maxUrlNumber != null && crawledCount >= crawlDefinition.maxUrlNumber)
			abort("Max URL number reached: " + crawlDefinition.maxUrlNumber);

		final DriverInterface.Content content = get.getContent();
		if (content == null)
			return; // No content ? We're done

		// If it is not HTML we're done
		if (!"text/html".equals(content.getContentType()))
			return;

		// Let's parse the HTML
		final Document document;
		try {
			try (final InputStream input = content.getInput()) {
				document = Jsoup.parse(input, content.getCharsetName(), uriString);
			}
		} catch (IOException e) {
			session.incErrorCount("Error during robots.txt extraction: " + e.getMessage());
			currentBuilder.error(e);
			return;
		}

		final Element body = document.body();
		if (body == null)
			return; // No body ? we are done

		timeTracker.next(null);
		for (final Element element : body.select("a[href]")) {
			final String href = element.attr("href");
			if (com.qwazr.utils.StringUtils.isBlank(href))
				continue;
			final String absHref = element.attr("abs:href");
			if (com.qwazr.utils.StringUtils.isBlank(absHref))
				continue;
			final URI newUri;
			try {
				newUri = new URI(absHref);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, e, () -> "Cannot build URI from " + absHref + ": " + e.getMessage());
				continue;
			}
			if (newUri.getHost() == null || newUri.getScheme() == null)
				continue;
			currentBuilder.link(transformLink(newUri));
		}
		timeTracker.next("Links extraction");
	}

	private RobotsTxt.Status checkRobotsTxt(final DriverInterface driver, final CurrentURIImpl.Builder currentBuilder)
			throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException {
		if (robotsTxtMap == null)
			return null;
		timeTracker.next(null);
		try {
			final URI robotsTxtURI = RobotsTxt.getRobotsURI(currentBuilder.uri);
			RobotsTxt robotsTxt = robotsTxtMap.get(robotsTxtURI);
			if (robotsTxt == null) {
				robotsTxt = RobotsTxt.download(driver, robotsTxtURI);
				robotsTxtMap.put(robotsTxtURI, robotsTxt);
			}
			return robotsTxt.getStatus(currentBuilder.uri, userAgent);
		} finally {
			timeTracker.next("Robots.txt check");
		}
	}

	private void crawlOne(final DriverInterface driver, final Set<URI> crawledURIs, final URI uri,
			final Collection<URI> nextLevelUris, final int depth) throws InterruptedException {

		if (session.isAborting())
			return;

		// Check if it has been already crawled
		if (crawledURIs != null) {
			if (crawledURIs.contains(uri))
				return;
			crawledURIs.add(uri);
		}

		// Do the crawl
		final CurrentURIImpl.Builder currentBuilder = new CurrentURIImpl.Builder(uri, depth);
		crawl(driver, currentBuilder);
		final CurrentURI current = currentBuilder.build();

		// Give the hand to the "crawl" event scripts
		script(EventEnum.crawl, current);

		// Add the next level URIs
		nextLevelUris.addAll(current.getLinks().keySet());

	}

	private void crawlSubLevel(final DriverInterface driver, final Set<URI> crawledURIs,
			final Collection<URI> levelURIs, final int depth) throws InterruptedException {

		if (crawlDefinition.maxDepth == null || depth > crawlDefinition.maxDepth)
			return;

		if (session.isAborting())
			return;

		if (levelURIs == null || levelURIs.isEmpty())
			return;

		final Set<URI> nextLevelURIs = new HashSet<>();

		// Crawl all URLs from the level
		for (URI uri : levelURIs)
			crawlOne(driver, crawledURIs, uri, nextLevelURIs, depth);

		// Let's crawl the next level if any
		crawlSubLevel(driver, crawledURIs, nextLevelURIs, depth + 1);
	}

	private void crawlStart(final DriverInterface driver, final Set<URI> crawledURIs, final String startUriString)
			throws URISyntaxException, InterruptedException {
		final URI startURI = new URI(startUriString);
		final Set<URI> nextLevelURIs = new HashSet<>();
		crawlOne(driver, crawledURIs, startURI, nextLevelURIs, 0);
		crawlSubLevel(driver, crawledURIs, nextLevelURIs, 1);
	}

	private void crawlUrlMap(final DriverInterface driver, final Set<URI> crawledURIs,
			final Map<String, Integer> urlMap) {
		urlMap.forEach((uriString, depth) -> {
			try {
				final URI uri = new URI(uriString);
				crawlOne(driver, crawledURIs, uri, null, depth == null ? 0 : depth);
			} catch (URISyntaxException e) {
				LOGGER.warning(() -> "Malformed URI: " + uriString);
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, e, () -> "Interruption on " + uriString);
			}
		});
	}

	protected void runner()
			throws URISyntaxException, IOException, ScriptException, ServerException, ReflectiveOperationException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
		try (final DriverInterface driver = DriverInterface.of(crawlDefinition)) {
			registerScriptGlobalObject("driver", driver);
			script(EventEnum.before_session, null);
			if (crawlDefinition.preUrl != null && !crawlDefinition.preUrl.isEmpty())
				driver.get(crawlDefinition.preUrl);
			final Set<URI> crawledURIs = new HashSet<>();
			if (crawlDefinition.urls != null && !crawlDefinition.urls.isEmpty())
				crawlUrlMap(driver, crawledURIs, crawlDefinition.urls);
			else {
				if (crawlDefinition.entryUrl != null)
					crawlStart(driver, crawledURIs, crawlDefinition.entryUrl);
				else if (crawlDefinition.entryRequest != null)
					throw new NotImplementedException("EntryRequest are not yet implemented");
			}
		} finally {
			script(EventEnum.after_session, null);
		}
	}

}
