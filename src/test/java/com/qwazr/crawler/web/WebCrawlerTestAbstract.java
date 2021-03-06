/*
 * Copyright 2016-2021 Emmanuel Keller / QWAZR
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

import com.qwazr.crawler.CrawlerServer;
import com.qwazr.crawler.common.CrawlCollectorTest;
import com.qwazr.crawler.common.CrawlHelpers;
import com.qwazr.crawler.common.CrawlSessionStatus;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.RandomUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class WebCrawlerTestAbstract {

    static WebCrawlerServiceInterface service;
    private static Path tempDatDir;

    public static void setup() throws Exception {
        tempDatDir = Files.createTempDirectory("test_data_dir");
        System.setProperty("QWAZR_DATA", tempDatDir.toAbsolutePath().toString());
        CrawlerServer.main();
        WebAppTestServer.start();
    }

    @AfterAll
    public static void cleanup() throws InterruptedException, IOException {
        WebAppTestServer.stop();
        CrawlerServer.shutdown();
        if (tempDatDir != null) {
            FileUtils.deleteDirectory(tempDatDir);
            tempDatDir = null;
        }
    }

    @Test
    @Order(200)
    public void test200emptySessions() {
        final Map<String, WebCrawlSessionStatus> sessions = service.getSessions(null, null, null,
                total -> assertThat(total, equalTo(0)));
        Assert.assertNotNull(sessions);
        assertThat(sessions.size(), equalTo(0));
    }

    private WebCrawlDefinition.Builder getNewWebCrawl() {
        final WebCrawlDefinition.Builder webCrawl = WebCrawlDefinition.of();
        webCrawl.setTimeOutSecs(60);
        webCrawl.setMaxUrlNumber(10);
        webCrawl.addAcceptedContentType("text/html");
        webCrawl.addAcceptedContentType("   image/vnd.microsoft.icon");
        webCrawl.setRobotsTxtEnabled(true);
        webCrawl.setDisableSslCheck(false);
        webCrawl.variable(RandomUtils.alphanumeric(5), RandomUtils.alphanumeric(6));
        webCrawl.setMaxDepth(2);
        return webCrawl;
    }

    private CrawlSessionStatus<?> crawlTest(String sessionName, WebCrawlDefinition webCrawl, int crawled, int rejected, int error)
            throws InterruptedException {
        final WebCrawlSessionStatus initialStatus = service.runSession(sessionName);
        assertThat(initialStatus, notNullValue());
        final WebCrawlDefinition initialDef = service.getSessionDefinition(sessionName);
        assertThat(initialDef, equalTo(webCrawl));
        final CrawlSessionStatus<?> status = CrawlHelpers.crawlWait(sessionName, service);
        Assert.assertEquals(crawled, status.crawled);
        Assert.assertEquals(rejected, status.rejected);
        Assert.assertEquals(error, status.error);
        Assert.assertEquals(status.running, Boolean.FALSE);
        Assert.assertNotNull(status.startTime);
        Assert.assertNotNull(status.endTime);
        return status;
    }

    private String newCrawlSession(WebCrawlDefinition webCrawl) {
        final String sessionName = RandomUtils.alphanumeric(10);
        final WebCrawlSessionStatus upsertStatus = service.upsertSession(sessionName, webCrawl);
        assertThat(upsertStatus, notNullValue());
        return sessionName;
    }

    private CrawlSessionStatus<?> crawlTest(WebCrawlDefinition webCrawl, int crawled, int rejected, int error)
            throws InterruptedException {
        final String sessionName = newCrawlSession(webCrawl);
        return crawlTest(sessionName, webCrawl, crawled, rejected, error);
    }

    @Test
    @Order(300)
    public void test300SimpleCrawlAndRecrawl() throws InterruptedException {
        final WebCrawlDefinition webCrawlDefinition = getNewWebCrawl().setEntryUrl(WebAppTestServer.URL).build();
        final String sessionName = newCrawlSession(webCrawlDefinition);
        //Crawl
        crawlTest(sessionName, webCrawlDefinition, 7, 1, 1);
        // Recrawl
        crawlTest(sessionName, webCrawlDefinition, 7, 1, 1);
    }

    @Test
    @Order(350)
    public void test350CrawlGetWebRequest() throws InterruptedException {
        final WebCrawlDefinition webCrawlDefinition = getNewWebCrawl()
                .setEntryRequest(
                        WebRequestDefinition
                                .of(WebAppTestServer.URL)
                                .httpMethod(WebRequestDefinition.HttpMethod.GET)
                                .build())
                .build();
        crawlTest(webCrawlDefinition, 7, 1, 1);
    }

    @Test
    public void test360CrawlPostWebRequest() throws InterruptedException {
        final WebCrawlDefinition webCrawlDefinition = getNewWebCrawl().setEntryRequest(
                WebRequestDefinition.of(WebAppTestServer.URL).httpMethod(WebRequestDefinition.HttpMethod.POST).build())
                .build();
        crawlTest(webCrawlDefinition, 1, 0, 1);
    }

    @Test
    @Order(400)
    public void test400CrawlEvent() throws InterruptedException {
        WebCrawlCollectorFactoryTest.resetCounters();

        final String sessionName = RandomUtils.alphanumeric(10);
        final WebCrawlDefinition.Builder webCrawl = getNewWebCrawl().setEntryUrl(WebAppTestServer.URL);
        webCrawl.crawlCollectorFactoryClass(WebCrawlCollectorFactoryTest.class);

        webCrawl.userAgent("QWAZR_BOT");
        service.upsertSession(sessionName, webCrawl.build());
        service.runSession(sessionName);
        final CrawlSessionStatus<?> status = CrawlHelpers.crawlWait(sessionName, service);

        Assert.assertEquals(7, status.crawled);
        Assert.assertEquals(1, status.rejected);
        Assert.assertEquals(1, status.redirect);
        Assert.assertEquals(1, status.error);

        assertThat(CrawlCollectorTest.all.size(), equalTo(8));
        assertThat(CrawlCollectorTest.getAll(URI.class).size(), equalTo(8));
        assertThat(CrawlCollectorTest.errors.size(), equalTo(1));
        assertThat(CrawlCollectorTest.rejecteds.size(), equalTo(1));

        WebCrawlCollectorFactoryTest.checkUri("http://localhost:9190", 0, 302, null);
        WebCrawlCollectorFactoryTest.checkUri("http://localhost:9190/index.html", 0, 200, 436);
        WebCrawlCollectorFactoryTest.checkUri("http://localhost:9190/private.html", 1, null, null);
        WebCrawlCollectorFactoryTest.checkUri("http://localhost:9190/page1.html", 1, 200, 45);
        WebCrawlCollectorFactoryTest.checkUri("http://localhost:9190/page2.html", 1, 200, 45);
        WebCrawlCollectorFactoryTest.checkUri("http://localhost:9190/favicon.ico", 1, 200, 22382);
        WebCrawlCollectorFactoryTest.checkUri("http://localhost:9190/page404.html", 1, 404, null);
        WebCrawlCollectorFactoryTest.checkUri("http://localhost:9190/favicon.ico?query=with+space", 1, 200, 22382);

        assertThat(WebCrawlCollectorFactoryTest.redirects,
                equalTo(Map.of(
                        URI.create("http://localhost:9190"),
                        URI.create("http://localhost:9190/index.html"))));

        assertThat(WebCrawlCollectorFactoryTest.uriRejected.keySet(),
                equalTo(Set.of(URI.create("http://localhost:9190/private.html"))));

        assertThat(CrawlCollectorTest.doneCalled.get(), equalTo(1));
    }

}
