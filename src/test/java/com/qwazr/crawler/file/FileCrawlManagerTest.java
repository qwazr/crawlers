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

import com.qwazr.crawler.common.WildcardFilter;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.RandomUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FileCrawlManagerTest {

    private static ExecutorService executorService;
    private static FileCrawlerManager crawlerManager;
    private static Path dataDir;

    @BeforeAll
    public static void setup() throws IOException {
        executorService = Executors.newCachedThreadPool();
        dataDir = Files.createTempDirectory("ftptest_data");
        crawlerManager = new FileCrawlerManager(dataDir, executorService);
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
                .addFilter("*" + File.separator, WildcardFilter.Status.accept)
                .addFilter("*.txt", WildcardFilter.Status.accept)
                .addFilter("*" + File.separator + "ignore.*", WildcardFilter.Status.reject)
                .addFilter("*" + File.separator + "ignore" + File.separator, WildcardFilter.Status.reject);
    }

    void crawlTest(final FileCrawlDefinition.Builder fileCrawlDefinitionBuilder,
                   final int expectedCrawled,
                   final int expectedIgnored,
                   final int expectedError) throws InterruptedException {
        final String crawlSession = RandomUtils.alphanumeric(5);
        FileCrawlStatus crawlStatus = crawlerManager.runSession(crawlSession, fileCrawlDefinitionBuilder.build());
        Assert.assertNotNull(crawlStatus);
        while (crawlStatus.endTime == null) {
            crawlStatus = crawlerManager.getSessionStatus(crawlSession);
            Thread.sleep(500);
        }
        Assert.assertEquals(expectedCrawled, crawlStatus.crawled);
        Assert.assertEquals(expectedIgnored, crawlStatus.rejected);
        Assert.assertEquals(expectedError, crawlStatus.error);
        Assert.assertNotNull(crawlStatus.threadDone);

        if (expectedError == 0)
            Assert.assertNull(crawlStatus.lastError);
        else
            Assert.assertNotNull(crawlStatus.lastError);

    }

    @Test
    public void crawlFullTest() throws InterruptedException {
        crawlTest(getFileCrawlDefinition(), 9, 2, 0);
    }

    @Test
    public void crawlDepth() throws InterruptedException {
        crawlTest(getFileCrawlDefinition().setMaxDepth(1), 4, 1, 0);
        crawlTest(getFileCrawlDefinition().setMaxDepth(2), 6, 2, 0);
        crawlTest(getFileCrawlDefinition().setMaxDepth(0), 1, 0, 0);
    }
}
