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
package com.qwazr.crawler.ftp;

import com.qwazr.crawler.common.CrawlHelpers;
import com.qwazr.crawler.common.CrawlSessionStatus;
import com.qwazr.crawler.common.WildcardFilter;
import com.qwazr.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FtpCrawlerTest {

    private FtpCrawlerServiceInterface ftpCrawler;
    private ExecutorService executorService;
    private Path dataDir;

    @BeforeEach
    public void setup() throws IOException {
        dataDir = Files.createTempDirectory("ftptest_data");
        executorService = Executors.newCachedThreadPool();
        FtpCrawlerManager ftpCrawlerManager = new FtpCrawlerManager(dataDir, executorService);
        ftpCrawler = ftpCrawlerManager.getService();
    }

    @Test
    public void test() throws InterruptedException {
        final FtpCrawlDefinition ftpCrawlDefinition = FtpCrawlDefinition.of()
                .hostname("ftp.mirrorservice.org")
                .entryPath("/sites/ftp.apache.org/commons/")
                .passive(true)
                .username("anonymous")
                .password("contact@qwazr.com")
                .setMaxDepth(2)
                .addFilter("/sites/ftp.apache.org/commons/", WildcardFilter.Status.accept)
                .addFilter("/sites/ftp.apache.org/commons/crypto/", WildcardFilter.Status.accept)
                .addFilter("/sites/ftp.apache.org/commons/*/*.html", WildcardFilter.Status.accept)
                .build();
        final FtpCrawlSessionStatus initialStatus = ftpCrawler.runSession("apache", ftpCrawlDefinition);
        assertThat(initialStatus, notNullValue());
        final FtpCrawlDefinition initialDef = ftpCrawler.getSessionDefinition("apache");
        assertThat(initialDef, equalTo(ftpCrawlDefinition));
        final CrawlSessionStatus<?> status = CrawlHelpers.crawlWait("apache", ftpCrawler);
        assertThat(status.crawled, equalTo(2));
        assertThat(status.rejected, Matchers.greaterThanOrEqualTo(3));
        Assert.assertEquals(0, status.error);
        Assert.assertNull(status.lastError);
    }

    @AfterEach
    public void cleanup() throws InterruptedException, IOException {
        if (executorService != null) {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);
            executorService = null;
        }
        FileUtils.deleteDirectory(dataDir);
    }

}
