/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.RandomUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import static org.hamcrest.CoreMatchers.equalTo;
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
        SortedMap<String, WebCrawlStatus> sessions = service.getSessions();
        Assert.assertNotNull(sessions);
        Assert.assertTrue(sessions.isEmpty());
    }

    private WebCrawlDefinition.Builder getNewWebCrawl() {
        final WebCrawlDefinition.Builder webCrawl = WebCrawlDefinition.of();
        webCrawl.setTimeOutSecs(60);
        webCrawl.setMaxUrlNumber(10);
        webCrawl.addAcceptedContentType("text/html");
        webCrawl.addAcceptedContentType("image/vnd.microsoft.icon");
        webCrawl.setRobotsTxtEnabled(true);
        webCrawl.setDisableSslCheck(false);
        webCrawl.variable(RandomUtils.alphanumeric(5), RandomUtils.alphanumeric(6));
        webCrawl.setMaxDepth(2);
        return webCrawl;
    }

    private CrawlStatus<?> crawlTest(WebCrawlDefinition webCrawl, int crawled, int ignored, int error)
            throws InterruptedException {
        final String sessionName = RandomUtils.alphanumeric(10);
        service.runSession(sessionName, webCrawl);
        final CrawlStatus<?> status = CrawlHelpers.crawlWait(sessionName, service);
        Assert.assertEquals(crawled, status.crawled);
        Assert.assertEquals(ignored, status.ignored);
        Assert.assertEquals(error, status.error);
        Assert.assertNotNull(status.threadDone);
        return status;
    }

    @Test
    @Order(300)
    public void test300SimpleCrawl() throws InterruptedException {
        crawlTest(getNewWebCrawl().setEntryUrl(WebAppTestServer.URL).build(), 5, 1, 1);
    }

    @Test
    @Order(350)
    public void test350CrawlGetWebRequest() throws InterruptedException {
        crawlTest(getNewWebCrawl().setEntryRequest(
                WebRequestDefinition.of(WebAppTestServer.URL).httpMethod(WebRequestDefinition.HttpMethod.GET).build())
                .build(), 5, 1, 1);
    }

    @Test
    public void test360CrawlPostWebRequest() throws InterruptedException {
        crawlTest(getNewWebCrawl().setEntryRequest(
                WebRequestDefinition.of(WebAppTestServer.URL).httpMethod(WebRequestDefinition.HttpMethod.POST).build())
                .build(), 0, 0, 1);
    }

    @Test
    @Order(400)
    public void test400CrawlEvent() throws InterruptedException {
        WebCrawlCollectorFactoryTest.resetCounters();

        final String sessionName = RandomUtils.alphanumeric(10);
        final WebCrawlDefinition.Builder webCrawl = getNewWebCrawl().setEntryUrl(WebAppTestServer.URL);
        webCrawl.crawlCollectorFactoryClass(WebCrawlCollectorFactoryTest.class);

        webCrawl.userAgent("QWAZR_BOT");
        service.runSession(sessionName, webCrawl.build());
        final CrawlStatus<?> status = CrawlHelpers.crawlWait(sessionName, service);

        Assert.assertEquals(5, status.crawled);
        Assert.assertEquals(1, status.ignored);
        Assert.assertEquals(1, status.redirect);
        Assert.assertEquals(1, status.error);

        assertThat(CrawlCollectorTest.count.size(), equalTo(8));
        assertThat(CrawlCollectorTest.crawled.size(), equalTo(5));
        assertThat(CrawlCollectorTest.ignored.size(), equalTo(1));
        assertThat(CrawlCollectorTest.inExclusion.size(), equalTo(0));
        assertThat(CrawlCollectorTest.inInclusion.size(), equalTo(0));

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

        assertThat(WebCrawlCollectorFactoryTest.robotsTxtRejected,
                equalTo(List.of(URI.create("http://localhost:9190/private.html"))));
    }

}
