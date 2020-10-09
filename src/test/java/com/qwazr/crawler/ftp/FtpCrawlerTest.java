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
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FtpCrawlerTest {

    private static FtpServer ftpServer;

    private FtpCrawlerServiceInterface ftpCrawler;
    private ExecutorService executorService;
    private Path dataDir;

    @BeforeAll
    public static void startFtpServer() throws FtpException {
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setServerAddress("localhost");
        listenerFactory.setPort(2221);
        serverFactory.addListener("default", listenerFactory.createListener());
        final PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        final UserManager userManager = userManagerFactory.createUserManager();
        final BaseUser user = new BaseUser();
        user.setName("anonymous");
        user.setPassword("contact@qwazr.com");
        userManager.save(user);
        serverFactory.setUserManager(userManager);
        ftpServer = serverFactory.createServer();
        ftpServer.start();
    }

    @AfterAll
    public static void stopFtpServer() {
        if (ftpServer != null && !ftpServer.isStopped()) {
            ftpServer.stop();
            ftpServer = null;
        }
    }

    @BeforeEach
    public void setup() throws IOException {
        dataDir = Files.createTempDirectory("ftptest_data");
        executorService = Executors.newCachedThreadPool();
        FtpCrawlerManager ftpCrawlerManager = new FtpCrawlerManager(dataDir, executorService);
        ftpCrawler = ftpCrawlerManager.getService();
    }

    @Test
    public void localFtpTest() throws InterruptedException {
        final FtpCrawlDefinition ftpCrawlDefinition = FtpCrawlDefinition.of()
                .hostname("localhost")
                .port(2221)
                .ssl(false)
                .entryPath(Path.of("src", "test", "java").toAbsolutePath().toString())
                .passive(true)
                .username("anonymous")
                .password("contact@qwazr.com")
                .setMaxDepth(2)
                .addFilter("*/src/test/java/com/", WildcardFilter.Status.accept)
                .addFilter("*/src/test/java/com/*/", WildcardFilter.Status.accept)
                .addFilter("*/src/test/java/com/qwazr/crawler/ftp/*.java", WildcardFilter.Status.accept)
                .build();
        final FtpCrawlSessionStatus initialStatus = ftpCrawler.runSession("localFtp", ftpCrawlDefinition);
        assertThat(initialStatus, notNullValue());
        final FtpCrawlDefinition initialDef = ftpCrawler.getSessionDefinition("localFtp");
        assertThat(initialDef, equalTo(ftpCrawlDefinition));
        final CrawlSessionStatus<?> status = CrawlHelpers.crawlWait("localFtp", ftpCrawler);
        assertThat(status.crawled, equalTo(1));
        assertThat(status.rejected, equalTo(15));
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
