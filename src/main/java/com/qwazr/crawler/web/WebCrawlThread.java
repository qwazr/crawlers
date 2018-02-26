/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import com.qwazr.crawler.common.CrawlThread;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.web.driver.DriverInterface;
import com.qwazr.crawler.web.robotstxt.RobotsTxt;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RegExpUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public class WebCrawlThread extends CrawlThread<WebCrawlDefinition, WebCrawlStatus, WebCrawlerManager> {

	private static final Logger LOGGER = LoggerUtils.getLogger(WebCrawlThread.class);

	private final WebCrawlDefinition crawlDefinition;

	private final List<Matcher> parametersMatcherList;
	private final List<Matcher> pathCleanerMatcherList;

	private final Map<URI, RobotsTxt> robotsTxtMap;
	private final String userAgent;

	private final Set<String> acceptedContentType;

	private final TimeTracker timeTracker;

	WebCrawlThread(final WebCrawlerManager webCrawlerManager, final WebCrawlSession session,
			final WebCrawlDefinition crawlDefinition) throws ServerException {
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

		if (crawlDefinition.acceptedContentType != null) {
			acceptedContentType = new HashSet<>();
			crawlDefinition.acceptedContentType.forEach(ct -> acceptedContentType.add(ct.toLowerCase()));
		} else
			acceptedContentType = null;

		userAgent = crawlDefinition.userAgent == null ? "QWAZR_BOT" : crawlDefinition.userAgent;
		final String u;
		try {
			u = crawlDefinition.entryUrl != null ? crawlDefinition.entryUrl : crawlDefinition.entryRequest.url;
			final URI uri = new URI(u);
			final String host = uri.getHost();
			if (host == null)
				throw new URISyntaxException(u, "No host found.", -1);
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
		try {
			final UBuilder uriBuilder = new UBuilder(uri);
			if (crawlDefinition.removeFragments != null && crawlDefinition.removeFragments)
				uriBuilder.removeFragment();
			if (parametersMatcherList != null && !parametersMatcherList.isEmpty())
				uriBuilder.removeMatchingParameters(parametersMatcherList);
			if (pathCleanerMatcherList != null && !pathCleanerMatcherList.isEmpty())
				uriBuilder.cleanPath(pathCleanerMatcherList);
			return uriBuilder.build();
		} catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.WARNING, e, () -> "Cannot build the URI from " + uri.toString());
			return null;
		}
	}

	private final void applyIgnore(final String uriString, final String msg, final CrawlUnit crawlUnit) {
		session.incIgnoredCount();
		crawlUnit.currentBuilder.ignored(true);
		LOGGER.info(() -> "Ignored (" + msg + "): " + uriString);
	}

	/**
	 * @param crawlUnit
	 */
	private DriverInterface.Body crawl(final CrawlUnit crawlUnit) throws InterruptedException {

		if (session.isAborting())
			return null;

		if (crawlDefinition.crawlWaitMs != null)
			Thread.sleep(crawlDefinition.crawlWaitMs);

		final DriverInterface.Body body = crawlUnit.crawl();
		if (body == null)
			return null; // Any error already handled by the crawler

		final DriverInterface.Content content = body.getContent();
		if (content == null)
			return body; // No content ? We're done

		if (!checkPassContentType(body.getContentType(), crawlUnit.currentBuilder)) {
			session.incIgnoredCount();
			applyIgnore(crawlUnit.currentBuilder.uriString, "rejected content-type: " + body.getContentType(),
					crawlUnit);
			return null;
		}

		crawlUnit.currentBuilder.crawled(true);
		crawlUnit.currentBuilder.body(body);

		// Let's parse the HTML if any
		final Document document;
		try {
			document = body.getHtmlDocument();
			if (document == null)
				return body; // No HTML document ? We're done
		} catch (IOException e) {
			session.incErrorCount("Error during body extraction: " + e.getMessage());
			crawlUnit.currentBuilder.error(e);
			return body;
		}

		final Element documentBody = document.body();
		if (documentBody == null)
			return body; // No body ? we are done

		timeTracker.next(null);
		for (final Element element : documentBody.select("a[href]")) {
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
			final URI linkUri = transformLink(newUri);
			if (linkUri != null) {
				crawlUnit.currentBuilder.link(linkUri);
				if (checkPassInclusionExclusion(linkUri.toString(), null, null))
					crawlUnit.currentBuilder.filteredLink(linkUri);
			}
		}
		timeTracker.next("Links extraction");
		return body;
	}

	private boolean checkPassContentType(final String contentType, final WebCurrentCrawlImpl.Builder currentBuilder) {
		final boolean accepted = acceptedContentType == null || acceptedContentType.contains(contentType);
		currentBuilder.contentType(contentType, !accepted);
		return accepted;
	}

	private RobotsTxt.Status checkRobotsTxt(final DriverInterface driver, final URI uri)
			throws IOException, URISyntaxException {
		if (robotsTxtMap == null)
			return null;
		timeTracker.next(null);
		try {
			final URI robotsTxtURI = RobotsTxt.getRobotsURI(uri);
			RobotsTxt robotsTxt = robotsTxtMap.get(robotsTxtURI);
			if (robotsTxt == null || robotsTxt.hasExpired(TimeUnit.HOURS, 6)) {
				robotsTxt = RobotsTxt.download(driver, robotsTxtURI);
				robotsTxtMap.put(robotsTxtURI, robotsTxt);
			}
			return robotsTxt.getStatus(uri, userAgent);
		} finally {
			timeTracker.next("Robots.txt check");
		}
	}

	private void crawlOne(final CrawlUnit crawlUnit, final Set<URI> crawledURIs, final Collection<URI> nextLevelUris)
			throws InterruptedException, URISyntaxException {

		if (session.isAborting())
			return;

		session.setCurrentCrawl(crawlUnit.currentBuilder.uriString, crawlUnit.currentBuilder.depth);

		// Check if it has been already crawled
		if (crawledURIs != null) {
			if (crawledURIs.contains(crawlUnit.currentBuilder.uri))
				return;
			crawledURIs.add(crawlUnit.currentBuilder.uri);
		}

		// Check the SCHEME, we only accept http or https
		final String scheme = crawlUnit.currentBuilder.uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
			applyIgnore(crawlUnit.currentBuilder.uriString, "Unsupported protocol: " + scheme, crawlUnit);

		// Check the inclusion/exclusion rules
		if (!checkPassInclusionExclusion(crawlUnit.currentBuilder.uriString, crawlUnit.currentBuilder::inInclusion,
				crawlUnit.currentBuilder::inExclusion))
			applyIgnore(crawlUnit.currentBuilder.uriString, "inclusion/exclusion", crawlUnit);

		// Check the robotsTxt status
		try {
			final RobotsTxt.Status robotsTxtStatus = checkRobotsTxt(crawlUnit.driver, crawlUnit.currentBuilder.uri);
			if (robotsTxtStatus != null && !robotsTxtStatus.isCrawlable) {
				crawlUnit.currentBuilder.robotsTxtDisallow(true);
				applyIgnore(crawlUnit.currentBuilder.uriString, "robotstxt", crawlUnit);
			}
		} catch (Exception e) {
			final String msg = "Error during robots.txt extraction: " + e.getMessage();
			LOGGER.log(Level.WARNING, msg, e);
			session.incErrorCount(msg);
			crawlUnit.currentBuilder.error(e);
		}

		// Call the before_crawl process:
		final WebCurrentCrawl beforeCrawlCurrent = crawlUnit.currentBuilder.build();
		if (!script(EventEnum.before_crawl, beforeCrawlCurrent))
			return;

		if (beforeCrawlCurrent.isIgnored() || beforeCrawlCurrent.getError() != null)
			return;

		final WebCurrentCrawl afterCrawlCurrent;

		try (final DriverInterface.Body body = crawl(crawlUnit)) {

			afterCrawlCurrent = crawlUnit.currentBuilder.build();

			// Handle url number limit
			if (afterCrawlCurrent.isCrawled()) {
				final int crawledCount = session.incCrawledCount();
				if (crawlDefinition.maxUrlNumber != null && crawledCount >= crawlDefinition.maxUrlNumber)
					abort("Max URL number reached: " + crawlDefinition.maxUrlNumber);
			}

			// Give the hand to the "crawl" event scripts
			script(EventEnum.after_crawl, afterCrawlCurrent);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e, e::getMessage);
			return;
		}

		// Manage any redirection
		final URI redirectUri = afterCrawlCurrent.getRedirect();
		if (redirectUri != null) {
			crawlOne(crawlUnit.redirect(redirectUri), crawledURIs, nextLevelUris);
			return;
		}

		// Add the next level URIs
		if (nextLevelUris != null) {
			final Map<URI, AtomicInteger> links = afterCrawlCurrent.getLinks();
			if (links != null)
				nextLevelUris.addAll(links.keySet());
		}
	}

	private void crawlSubLevel(final CrawlUnit crawlUnit, final Set<URI> crawledURIs, final Collection<URI> levelURIs,
			final int depth) throws InterruptedException, URISyntaxException {

		if (crawlDefinition.maxDepth == null || depth > crawlDefinition.maxDepth)
			return;

		if (session.isAborting())
			return;

		if (levelURIs == null || levelURIs.isEmpty())
			return;

		final Set<URI> nextLevelURIs = new HashSet<>();

		// Crawl all URLs from the level
		for (URI uri : levelURIs)
			crawlOne(crawlUnit.next(uri, depth), crawledURIs, nextLevelURIs);

		// Let's crawl the next level if any
		crawlSubLevel(crawlUnit, crawledURIs, nextLevelURIs, depth + 1);
	}

	private void crawlStart(final CrawlUnit crawlUnit, final Set<URI> crawledURIs)
			throws URISyntaxException, InterruptedException {
		final Set<URI> nextLevelURIs = new HashSet<>();
		crawlOne(crawlUnit, crawledURIs, nextLevelURIs);
		crawlSubLevel(crawlUnit, crawledURIs, nextLevelURIs, crawlUnit.currentBuilder.depth + 1);
	}

	private void crawlUrlMap(final DriverInterface driver, final Set<URI> crawledURIs,
			final Map<String, Integer> urlMap) {
		urlMap.forEach((uriString, depth) -> {
			try {
				crawlOne(new Get(driver, WebRequestDefinition.of(uriString).build(), depth == null ? 0 : depth),
						crawledURIs, null);
			} catch (URISyntaxException e) {
				LOGGER.warning(() -> "Malformed URI: " + uriString);
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, e, () -> "Interruption on " + uriString);
			}
		});
	}

	protected void runner() throws URISyntaxException, IOException, ServerException, InterruptedException {
		try (final DriverInterface driver = DriverInterface.of(crawlDefinition)) {
			registerScriptGlobalObject("driver", driver);
			script(EventEnum.before_session, null);
			if (crawlDefinition.preUrl != null && !crawlDefinition.preUrl.isEmpty())
				driver.body(WebRequestDefinition.of(crawlDefinition.preUrl).build()).close();
			final Set<URI> crawledURIs = new HashSet<>();
			if (crawlDefinition.urls != null && !crawlDefinition.urls.isEmpty())
				crawlUrlMap(driver, crawledURIs, crawlDefinition.urls);
			else {
				if (crawlDefinition.entryUrl != null)
					crawlStart(new Get(driver, WebRequestDefinition.of(crawlDefinition.entryUrl).build(), 0),
							crawledURIs);
				else if (crawlDefinition.entryRequest != null)
					crawlStart(crawlUnit(driver, crawlDefinition.entryRequest), crawledURIs);
			}
		} finally {
			script(EventEnum.after_session, null);
		}
	}

	private CrawlUnit crawlUnit(DriverInterface driver, WebRequestDefinition webRequest) throws URISyntaxException {
		Objects.requireNonNull(webRequest.url, "WebRequest failure: The URL is missing");
		switch (Objects.requireNonNull(webRequest.method, "WebRequest failure: The method is missing")) {
		case GET:
			return new Get(driver, webRequest, 0);
		case PUT:
		case POST:
			return new RequestAndGet(driver, webRequest, 0);
		default:
			throw new NotImplementedException("WebRequest failure: Method not supported: " + webRequest.method);
		}
	}

	@FunctionalInterface
	interface Method<T extends DriverInterface.Head> {
		T apply() throws IOException;
	}

	abstract class CrawlUnit {

		final WebCurrentCrawlImpl.Builder currentBuilder;
		final DriverInterface driver;
		final WebRequestDefinition request;

		CrawlUnit(final DriverInterface driver, final WebRequestDefinition request, final int depth)
				throws URISyntaxException {
			this.driver = driver;
			this.currentBuilder = new WebCurrentCrawlImpl.Builder(new URI(request.url), depth);
			this.request = request;
		}

		abstract CrawlUnit redirect(final URI uri) throws URISyntaxException;

		abstract CrawlUnit next(final URI uri, int depth) throws URISyntaxException;

		abstract DriverInterface.Body crawl();

		<T extends DriverInterface.Head> T checkHttp(Method<T> method) {
			try {
				timeTracker.next(null);

				final T methodResult = method.apply();
				currentBuilder.statusCode(methodResult.getResponseCode());

				final String redirectLocation = methodResult.getRedirectLocation();
				if (!StringUtils.isBlank(redirectLocation)) {
					final URI redirectUri = new URI(redirectLocation);
					session.incRedirectCount();
					currentBuilder.redirect(redirectUri);
					return null;
				}
				if (!methodResult.isSuccessful()) {
					final String msg = "Wrong HTTP code: " + methodResult.getResponseCode();
					session.incErrorCount("Error on " + currentBuilder.uriString + ": " + msg);
					currentBuilder.error(msg);
					return null;
				}

				if (!checkPassContentType(methodResult.getContentType(), currentBuilder)) {
					session.incIgnoredCount();
					return null;
				}

				return methodResult;
			} catch (Exception e) {
				final String msg = "Error on " + currentBuilder.uriString + ": " + e.getMessage();
				LOGGER.log(Level.WARNING, msg, e);
				session.incErrorCount(msg);
				currentBuilder.error(e);
				return null;
			} finally {
				timeTracker.next("HTTP");
			}
		}
	}

	class Get extends CrawlUnit {

		Get(final DriverInterface driver, final WebRequestDefinition request, final int depth)
				throws URISyntaxException {
			super(driver, request, depth);
		}

		@Override
		CrawlUnit redirect(final URI redirectUri) throws URISyntaxException {
			return new Get(driver, WebRequestDefinition.of(request).url(redirectUri).build(), currentBuilder.depth);
		}

		@Override
		final CrawlUnit next(final URI nextURI, int depth) throws URISyntaxException {
			return new Get(driver, WebRequestDefinition.of(request).url(nextURI).build(), depth);
		}

		@Override
		DriverInterface.Body crawl() {
			return checkHttp(() -> driver.body(request));
		}
	}

	final class RequestAndGet extends Get {

		RequestAndGet(final DriverInterface driver, final WebRequestDefinition request, final int depth)
				throws URISyntaxException {
			super(driver, request, depth);
		}

		@Override
		CrawlUnit redirect(URI redirectUri) throws URISyntaxException {
			return new RequestAndGet(driver, WebRequestDefinition.of(request).url(redirectUri).build(),
					currentBuilder.depth);
		}

		@Override
		DriverInterface.Body crawl() {
			return checkHttp(() -> driver.body(request));
		}
	}

}
