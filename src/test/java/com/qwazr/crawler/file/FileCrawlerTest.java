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
import com.qwazr.crawler.common.CommonEvent;
import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.server.RemoteService;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.RandomUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileCrawlerTest {

    private static FileCrawlerServiceInterface local;
    private static FileCrawlerServiceInterface remote;
    private static Path tempDatDir;

    @BeforeClass
    public static void before() throws Exception {
        tempDatDir = Files.createTempDirectory("test_data_dir");
        System.setProperty("QWAZR_DATA", tempDatDir.toAbsolutePath().toString());
        CrawlerServer.main();
    }

    @AfterClass
    public static void after() throws IOException {
        CrawlerServer.shutdown();
        if (tempDatDir != null) {
            FileUtils.deleteDirectory(tempDatDir);
            tempDatDir = null;
        }
    }

    @Test
    public void test100startServer() throws Exception {
        local = CrawlerServer.getInstance().getFileCrawlerServiceBuilder().local();
        Assert.assertNotNull(local);
        remote = new FileCrawlerSingleClient(RemoteService.of("http://localhost:9091").build());
        Assert.assertNotNull(remote);
    }

    @Test
    public void test200emptySessions() {
        SortedMap<String, FileCrawlStatus> sessions = remote.getSessions();
        Assert.assertNotNull(sessions);
        Assert.assertTrue(sessions.isEmpty());
    }

    private FileCrawlDefinition.Builder getNewCrawl() {
        return FileCrawlDefinition.of()
                .entryPath(Paths.get("src", "test", "file_crawl").toString())
                .addInclusionPattern("*" + File.separator)
                .addInclusionPattern("*.txt")
                .addExclusionPattern("*" + File.separator + "ignore" + File.separator)
                .addExclusionPattern("*" + File.separator + "ignore.*")
                .setCrawlWaitMs(10)
                .setMaxDepth(10);
    }

    @Test
    public void test300SimpleCrawl() throws InterruptedException {
        final String sessionName = RandomUtils.alphanumeric(10);
        remote.runSession(sessionName, getNewCrawl().build());
        final CrawlStatus status = CommonEvent.crawlWait(sessionName, remote);
        Assert.assertEquals(7, status.crawled);
        Assert.assertEquals(2, status.ignored);
        Assert.assertEquals(0, status.error);
        Assert.assertNull(status.lastError);
    }

    @Test
    public void test400CrawlEvent() throws InterruptedException {
        FileEvents.feedbacks.clear();
        final String sessionName = RandomUtils.alphanumeric(10);
        final FileCrawlDefinition.Builder crawl = getNewCrawl();
        final String variableName = RandomUtils.alphanumeric(5);
        final String variableValue = RandomUtils.alphanumeric(6);
        crawl.script(EventEnum.before_crawl, ScriptDefinition.of(FileEvents.BeforeCrawl.class)
                .variable(variableName, variableValue + EventEnum.before_crawl.name())
                .build());
        crawl.script(EventEnum.after_crawl, ScriptDefinition.of(FileEvents.AfterCrawl.class)
                .variable(variableName, variableValue + EventEnum.after_crawl.name())
                .build());
        crawl.script(EventEnum.before_session, ScriptDefinition.of(FileEvents.BeforeSession.class)
                .variable(variableName, variableValue + EventEnum.before_session.name())
                .build());
        crawl.script(EventEnum.after_session, ScriptDefinition.of(FileEvents.AfterSession.class)
                .variable(variableName, variableValue + EventEnum.after_session.name())
                .build());
        remote.runSession(sessionName, crawl.build());
        CommonEvent.crawlWait(sessionName, remote);
        Assert.assertEquals(9, FileEvents.feedbacks.get(EventEnum.before_crawl).count());
        Assert.assertEquals(7, FileEvents.feedbacks.get(EventEnum.after_crawl).count());
        Assert.assertEquals(1, FileEvents.feedbacks.get(EventEnum.before_session).count());
        Assert.assertEquals(1, FileEvents.feedbacks.get(EventEnum.after_session).count());
        for (EventEnum eventEnum : EventEnum.values())
            Assert.assertEquals(variableValue + eventEnum.name(),
                    FileEvents.feedbacks.get(eventEnum).variable(variableName));

        CommonEvent.Feedback<FileCrawlDefinition, FileCurrentPath> afterFeedback =
                FileEvents.feedbacks.get(EventEnum.after_crawl);
        Assert.assertEquals(0, afterFeedback.crawlDepth("src" + File.separator + "test" + File.separator + "file_crawl" + File.separator), 0);
        Assert.assertEquals(1, afterFeedback.crawlDepth("src" + File.separator + "test" + File.separator + "file_crawl" + File.separator + "file0.txt"), 0);
        Assert.assertEquals(1, afterFeedback.crawlDepth("src" + File.separator + "test" + File.separator + "file_crawl" + File.separator + "dir1" + File.separator), 0);
        Assert.assertEquals(2, afterFeedback.crawlDepth("src" + File.separator + "test" + File.separator + "file_crawl" + File.separator + "dir1" + File.separator + "subdir" + File.separator), 0);
        Assert.assertEquals(3, afterFeedback.crawlDepth("src" + File.separator + "test" + File.separator + "file_crawl" + File.separator + "dir1" + File.separator + "subdir" + File.separator + "file1.txt"), 0);
        Assert.assertEquals(1, afterFeedback.crawlDepth("src" + File.separator + "test" + File.separator + "file_crawl" + File.separator + "dir2" + File.separator), 0);
        Assert.assertEquals(2, afterFeedback.crawlDepth("src" + File.separator + "test" + File.separator + "file_crawl" + File.separator + "dir2" + File.separator + "file2.txt"), 0);
        CommonEvent.Feedback<FileCrawlDefinition, FileCurrentPath> beforeFeedback =
                FileEvents.feedbacks.get(EventEnum.before_crawl);
        Assert.assertEquals(2, beforeFeedback.crawlDepth("src" + File.separator + "test" + File.separator + "file_crawl" + File.separator + "dir2" + File.separator + "ignore.txt"), 0);
        Assert.assertEquals(1, beforeFeedback.crawlDepth("src" + File.separator + "test" + File.separator + "file_crawl" + File.separator + "ignore" + File.separator), 0);
    }

}
