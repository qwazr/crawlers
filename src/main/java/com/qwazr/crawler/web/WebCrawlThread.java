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
import com.qwazr.crawler.common.Rejected;
import com.qwazr.crawler.web.driver.DriverInterface;
import com.qwazr.crawler.web.robotstxt.RobotsTxt;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RegExpUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebCrawlThread extends CrawlThread
        <WebCrawlThread, WebCrawlDefinition, WebCrawlSessionStatus, WebCrawlerManager, WebCrawlSession, WebCrawlItem> {

    private static final Logger LOGGER = LoggerUtils.getLogger(WebCrawlThread.class);

    private final WebCrawlDefinition crawlDefinition;

    private final List<Matcher> parametersMatcherList;
    private final List<Matcher> pathCleanerMatcherList;

    private final Map<URI, RobotsTxt> robotsTxtMap;
    private final String userAgent;

    private final Set<String> acceptedContentType;

    private final TimeTracker timeTracker;

    WebCrawlThread(final WebCrawlerManager webCrawlerManager,
                   final WebCrawlSession session,
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

    DriverInterface.Body executeHttp(final DriverInterface driver,
                                     final WebRequestDefinition request,
                                     final WebCrawlItemImpl.Builder builder) {
        try {
            timeTracker.next(null);

            final DriverInterface.Body body = driver.body(request);
            builder.statusCode(body.getResponseCode());

            final String redirectLocation = body.getRedirectLocation();
            if (!StringUtils.isBlank(redirectLocation)) {
                final URI redirectUri = new URI(redirectLocation);
                builder.redirect(redirectUri);
                return null;
            }
            if (!body.isSuccessful()) {
                builder.error("Error on " + builder.uriString + ": Wrong HTTP code: " + body.getResponseCode());
                return null;
            }

            if (acceptedContentType != null && !acceptedContentType.contains(body.getContentType())) {
                builder.rejected(new Rejected(15, "Rejected content type"));
                return null;
            }

            return body;
        } catch (Exception e) {
            final String msg = "Error on " + builder.uriString + ": " + e.getMessage();
            LOGGER.log(Level.WARNING, msg, e);
            builder.error(e);
            return null;
        } finally {
            timeTracker.next("HTTP");
        }
    }

    private DriverInterface.Body crawlBody(final DriverInterface driver,
                                           final WebRequestDefinition request,
                                           final WebCrawlItemImpl.Builder builder) throws InterruptedException {

        if (session.isAborting())
            return null;

        if (crawlDefinition.crawlWaitMs != null)
            Thread.sleep(crawlDefinition.crawlWaitMs);

        final DriverInterface.Body body = executeHttp(driver, request, builder);
        if (body == null)
            return null; // Any error already handled by the crawler

        final DriverInterface.Content content = body.getContent();
        if (content == null)
            return body; // No content ? We're done

        builder.body(body);

        // Let's parse the HTML if any
        final Document document;
        try {
            document = body.getHtmlDocument();
            if (document == null)
                return body; // No HTML document ? We're done
        } catch (IOException e) {
            builder.error("Error during body extraction: " + e.getMessage());
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
                newUri = new URL(absHref.trim()
                        .replace(' ', '+')
                        .replace("|", "%7C"))
                        .toURI();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e, () -> "Cannot build URI from " + absHref + ": " + e.getMessage());
                continue;
            }
            if (newUri.getHost() == null || newUri.getScheme() == null)
                continue;
            final URI linkUri = transformLink(newUri);
            if (linkUri != null) {
                builder.link(linkUri);
                if (crawlDefinition.maxDepth == null || builder.depth < crawlDefinition.maxDepth)
                    if (checkWildcardFilters(linkUri.toString()) == null)
                        builder.filteredLink(linkUri);
            }
        }
        timeTracker.next("Links extraction");
        return body;
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

    private void crawlRequest(final DriverInterface driver,
                              final WebRequestDefinition request,
                              final WebCrawlItemImpl.Builder builder,
                              final AtomicBoolean collected)
            throws InterruptedException {

        // Check the scheme, we only accept HTTP or HTTPS
        final String scheme = builder.item.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            builder.rejected(new Rejected(10, "Unsupported protocol: " + scheme));
            return;
        }

        // Check the inclusion/exclusion rules
        final Rejected rejected = checkWildcardFilters(builder.uriString);
        if (rejected != null) {
            builder.rejected(rejected);
            return;
        }

        // Check the robotsTxt status
        try {
            final RobotsTxt.Status robotsTxtStatus = checkRobotsTxt(driver, builder.item);
            if (robotsTxtStatus != null && !robotsTxtStatus.isCrawlable) {
                builder.rejected(new Rejected(20, "RobotsTxt"));
                return;
            }
        } catch (Exception e) {
            final String msg = "Error during robots.txt extraction: " + e.getMessage();
            LOGGER.log(Level.WARNING, msg, e);
            builder.error(e);
            return;
        }


        try (final DriverInterface.Body body = crawlBody(driver, request, builder)) {

            // Handle url number limit
            final int crawledCount = session.incCrawledCount();
            if (crawlDefinition.maxUrlNumber != null && crawledCount >= crawlDefinition.maxUrlNumber)
                abort("Max URL number reached: " + crawlDefinition.maxUrlNumber);

            // Give the hand to the "crawl" event scripts
            session.collect(builder.build());
            collected.set(true);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e, e::getMessage);
            return;
        }

        final WebCrawlItem afterCrawlCurrent = builder.build();
        // Manage any redirection
        final URI redirectUri = afterCrawlCurrent.getRedirect();
        if (redirectUri != null) {
            session.addUrltoCrawl(redirectUri, builder.depth);
            return;
        }

        // Add the next level URIs
        final Map<URI, AtomicInteger> links = afterCrawlCurrent.getLinks();
        if (links != null && (crawlDefinition.maxDepth == null || builder.depth < crawlDefinition.maxDepth))
            session.addUrlsToCrawl(links.keySet(), builder.depth + 1);
    }

    private void crawlOne(final DriverInterface driver,
                          final WebRequestDefinition webRequest,
                          final int depth) throws InterruptedException {
        if (session.isAborting())
            return;
        final URI uri;
        try {
            uri = new URI(webRequest.url);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.INFO, "URI syntax error: " + webRequest.url, e);
            return;
        }

        final WebCrawlItemImpl.Builder builder = new WebCrawlItemImpl.Builder(uri, depth);
        // Check if it has been already crawled
        if (session.isCrawled(builder.uriString))
            return;
        session.setCrawled(builder.uriString, builder.depth);
        WebCrawlItem crawlItem = null;
        final AtomicBoolean collected = new AtomicBoolean(false);
        try {
            crawlRequest(driver, webRequest, builder, collected);
            crawlItem = builder.build();
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            builder.error(e);
        }
        if (crawlItem == null)
            crawlItem = builder.build();
        if (!collected.get())
            session.collect(crawlItem);
        final String error = crawlItem.getError();
        if (error != null)
            session.incErrorCount(error);
        final Rejected rejected = crawlItem.getRejected();
        if (rejected != null)
            session.incRejectedCount();
        if (crawlItem.getRedirect() != null)
            session.incRedirectCount();
    }

    private void crawlRemaining(final DriverInterface driver) throws InterruptedException {
        while (!session.isAborting()) {
            final Pair<String, Integer> nextUrl = session.nextUrlToCrawl();
            if (nextUrl == null)
                break;
            crawlOne(driver, WebRequestDefinition.of(nextUrl.getKey()).build(), nextUrl.getValue());
        }
    }

    private void crawlStart(final DriverInterface driver, WebRequestDefinition webRequest) throws
            InterruptedException {
        Objects.requireNonNull(webRequest.url, "WebRequest failure: The URL is missing");
        Objects.requireNonNull(webRequest.method, "WebRequest failure: The method is missing");
        crawlOne(driver, webRequest, 0);
        crawlRemaining(driver);
    }

    private void crawlUrlMap(final DriverInterface driver,
                             final Map<String, Integer> urlMap) throws InterruptedException {
        session.addUrlsToCrawl(urlMap);
        crawlRemaining(driver);
    }

    protected void runner() throws IOException, InterruptedException {
        try (final DriverInterface driver = DriverInterface.of(crawlDefinition)) {
            if (crawlDefinition.urls != null && !crawlDefinition.urls.isEmpty()) {
                crawlUrlMap(driver, crawlDefinition.urls);
                return;
            }
            if (crawlDefinition.entryUrl != null) {
                crawlStart(driver, WebRequestDefinition.of(crawlDefinition.entryUrl).build());
            } else if (crawlDefinition.entryRequest != null) {
                crawlStart(driver, crawlDefinition.entryRequest);
            }
        }
    }


}
