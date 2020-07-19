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
package com.qwazr.crawler.file;

import com.qwazr.crawler.CrawlerServer;
import com.qwazr.crawler.common.CrawlCollectorTest;
import com.qwazr.crawler.common.CrawlHelpers;
import com.qwazr.crawler.common.CrawlSessionStatus;
import com.qwazr.crawler.common.WildcardFilter;
import com.qwazr.crawler.ftp.FtpCrawlDefinition;
import com.qwazr.server.RemoteService;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.RandomUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileCrawlerTest {

    private static FileCrawlerServiceInterface local;
    private static FileCrawlerServiceInterface remote;
    private static Path tempDatDir;

    @BeforeAll
    public static void before() throws Exception {
        tempDatDir = Files.createTempDirectory("test_data_dir");
        System.setProperty("QWAZR_DATA", tempDatDir.toAbsolutePath().toString());
        CrawlerServer.main();

        local = CrawlerServer.getInstance().getFileCrawlerServiceBuilder().local();
        Assert.assertNotNull(local);
        remote = new FileCrawlerSingleClient(RemoteService.of("http://localhost:9091").build());
        Assert.assertNotNull(remote);
    }

    @AfterAll
    public static void after() throws IOException {
        CrawlerServer.shutdown();
        if (tempDatDir != null) {
            FileUtils.deleteDirectory(tempDatDir);
            tempDatDir = null;
        }
    }

    @Test
    @Order(200)
    public void test200emptySessions() {
        SortedMap<String, FileCrawlSessionStatus> sessions = remote.getSessions();
        Assert.assertNotNull(sessions);
        Assert.assertTrue(sessions.isEmpty());
    }

    private FileCrawlDefinition.Builder getNewCrawl() {
        return FileCrawlDefinition.of()
                .entryPath(Paths.get("src", "test", "file_crawl").toString())
                .addFilter("*" + File.separator + "ignore" + File.separator, WildcardFilter.Status.reject)
                .addFilter("*" + File.separator + "ignore.*", WildcardFilter.Status.reject)
                .addFilter("*" + File.separator, WildcardFilter.Status.accept)
                .addFilter("*.txt", WildcardFilter.Status.accept)
                .setCrawlWaitMs(10)
                .setMaxDepth(10);
    }

    @Test
    @Order(300)
    public void test300SimpleCrawl() throws InterruptedException {
        final String sessionName = RandomUtils.alphanumeric(10);
        final FileCrawlDefinition fileCrawl = getNewCrawl().build();
        final FileCrawlSessionStatus initialStatus = remote.runSession(sessionName, fileCrawl);
        assertThat(initialStatus, notNullValue());
        final FileCrawlDefinition initialDef = remote.getSessionDefinition(sessionName);
        assertThat(initialDef, equalTo(fileCrawl));
        final CrawlSessionStatus<?> status = CrawlHelpers.crawlWait(sessionName, remote);
        Assert.assertEquals(9, status.crawled);
        Assert.assertEquals(2, status.rejected);
        Assert.assertEquals(0, status.error);
        Assert.assertNull(status.lastError);
    }

    @Test
    @Order(400)
    public void test400CrawlEvent() throws InterruptedException {
        FileCrawlCollectorFactoryTest.resetCounters();
        final String sessionName = RandomUtils.alphanumeric(10);
        final FileCrawlDefinition.Builder crawl = getNewCrawl();
        crawl.variable(RandomUtils.alphanumeric(5), RandomUtils.alphanumeric(6));
        crawl.crawlCollectorFactoryClass(FileCrawlCollectorFactoryTest.class);
        remote.runSession(sessionName, crawl.build());
        CrawlHelpers.crawlWait(sessionName, remote);

        Assert.assertEquals(crawl.build(), FileCrawlCollectorFactoryTest.definition.get());

        assertThat(CrawlCollectorTest.getAll(Path.class).size(), equalTo(9));
        assertThat(CrawlCollectorTest.rejecteds.size(), equalTo(2));

        final Map<Path, Integer> result = new HashMap<>();
        result.put(Path.of("src/test/file_crawl"), 0);
        result.put(Path.of("src/test/file_crawl/file0.txt"), 1);
        result.put(Path.of("src/test/file_crawl/dir2"), 1);
        result.put(Path.of("src/test/file_crawl/dir2/file2.txt"), 2);
        result.put(Path.of("src/test/file_crawl/dir2/ignore.txt"), 2);
        result.put(Path.of("src/test/file_crawl/ignore"), 1);
        result.put(Path.of("src/test/file_crawl/dir1"), 1);
        result.put(Path.of("src/test/file_crawl/dir1/subdir"), 2);
        result.put(Path.of("src/test/file_crawl/dir1/subdir/file1.txt"), 3);
        assertThat(FileCrawlCollectorFactoryTest.pathDepth, equalTo(result));
    }

}
