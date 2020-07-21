/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.crawler.common.CrawlCollectorTest;
import com.qwazr.crawler.common.Rejected;
import com.qwazr.crawler.common.WildcardFilter;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.RandomUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FileCrawlManagerTest {

    private static ExecutorService executorService;
    private static FileCrawlerManager crawlerManager;
    private static Path dataDir;
    private static Instant instantAttribute;

    @BeforeAll
    public static void setup() throws IOException {
        executorService = Executors.newCachedThreadPool();
        dataDir = Files.createTempDirectory("ftptest_data");
        crawlerManager = new FileCrawlerManager(dataDir, executorService);
        instantAttribute = Instant.now();
        crawlerManager.registerAttribute("instant", instantAttribute);
    }

    @AfterAll
    public static void cleanup() throws IOException {
        if (executorService != null)
            executorService.shutdown();
        if (dataDir != null)
            FileUtils.deleteDirectory(dataDir);
    }

    FileCrawlDefinition.Builder getFileCrawlDefinition() {
        return FileCrawlDefinition.of()
                .entryPath(Paths.get("src", "test", "file_crawl").toString())
                .crawlCollectorFactoryClass(FileCrawlCollectorFactoryTest.class)
                .addFilter("*" + File.separator + "ignore.*", WildcardFilter.Status.reject)
                .addFilter("*" + File.separator + "ignore" + File.separator, WildcardFilter.Status.reject)
                .addFilter("*" + File.separator, WildcardFilter.Status.accept)
                .addFilter("*.txt", WildcardFilter.Status.accept);
    }

    void crawlTest(final FileCrawlDefinition.Builder fileCrawlDefinitionBuilder,
                   final int expectedCrawled,
                   final int expectedIgnored,
                   final int expectedError) throws InterruptedException {
        FileCrawlCollectorFactoryTest.resetCounters();
        final String crawlSession = RandomUtils.alphanumeric(5);
        FileCrawlSessionStatus crawlStatus = crawlerManager.runSession(crawlSession, fileCrawlDefinitionBuilder.build());
        Assert.assertNotNull(crawlStatus);
        while (crawlStatus.endTime == null) {
            Thread.sleep(500);
            crawlStatus = crawlerManager.getSessionStatus(crawlSession);
        }
        Assert.assertEquals(crawlStatus.toString(), expectedCrawled, crawlStatus.crawled);
        Assert.assertEquals(crawlStatus.toString(), expectedIgnored, crawlStatus.rejected);
        Assert.assertEquals(crawlStatus.toString(), expectedError, crawlStatus.error);

        if (expectedError == 0)
            Assert.assertNull(crawlStatus.lastError);
        else
            Assert.assertNotNull(crawlStatus.lastError);
        assertThat(FileCrawlCollectorFactoryTest.attributes.get(), notNullValue());
        assertThat(FileCrawlCollectorFactoryTest.attributes.get().getInstance("instant", Instant.class),
                equalTo(instantAttribute));

    }

    @Test
    public void crawlFullTest() throws InterruptedException {
        crawlTest(getFileCrawlDefinition(), 9, 2, 0);
        assertThat(CrawlCollectorTest.getAll(Path.class), equalTo(Set.of(
                Path.of("src/test/file_crawl"),
                Path.of("src/test/file_crawl/file0.txt"),
                Path.of("src/test/file_crawl/dir2"),
                Path.of("src/test/file_crawl/dir2/file2.txt"),
                Path.of("src/test/file_crawl/dir2/ignore.txt"),
                Path.of("src/test/file_crawl/ignore"),
                Path.of("src/test/file_crawl/dir1"),
                Path.of("src/test/file_crawl/dir1/subdir"),
                Path.of("src/test/file_crawl/dir1/subdir/file1.txt"))));
        assertThat(CrawlCollectorTest.rejecteds, equalTo(Map.of(
                Path.of("src/test/file_crawl/dir2/ignore.txt"), Rejected.WILDCARD_FILTER,
                Path.of("src/test/file_crawl/ignore"), Rejected.WILDCARD_FILTER)));
    }

    @Test
    public void crawlDepth() throws InterruptedException {
        crawlTest(getFileCrawlDefinition().setMaxDepth(1), 5, 1, 0);
        crawlTest(getFileCrawlDefinition().setMaxDepth(2), 8, 2, 0);
        crawlTest(getFileCrawlDefinition().setMaxDepth(0), 1, 0, 0);
    }
}
