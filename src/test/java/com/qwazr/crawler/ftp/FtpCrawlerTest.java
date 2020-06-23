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

import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.common.ScriptDefinition;
import com.qwazr.scripts.ScriptManager;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;

public class FtpCrawlerTest {

    private FtpCrawlerServiceInterface ftpCrawler;
    private ExecutorService executorService;
    private static final Object attributeTest = new Object();

    @Before
    public void setup() {
        executorService = Executors.newCachedThreadPool();
        ScriptManager scriptManager = new ScriptManager(executorService, Paths.get("."));
        FtpCrawlerManager ftpCrawlerManager = new FtpCrawlerManager(scriptManager, executorService);
        ftpCrawlerManager.setAttribute("attributeTest", attributeTest, Object.class);
        ftpCrawler = ftpCrawlerManager.getService();
    }

    @Test
    public void test() {
        FtpCrawlDefinition ftpCrawlDefinition = FtpCrawlDefinition.of()
                .hostname("ftp.mirrorservice.org")
                .entryPath("/sites/ftp.apache.org/commons/")
                .passive(true)
                .username("anonymous")
                .password("contact@qwazr.com")
                .addInclusionPattern("/sites/ftp.apache.org/commons")
                .addInclusionPattern("/sites/ftp.apache.org/commons/*")
                .addInclusionPattern("/sites/ftp.apache.org/commons/*/binaries/*")
                .addInclusionPattern("/sites/ftp.apache.org/commons/*/binaries/*/*.zip")
                .script(EventEnum.after_crawl, ScriptDefinition.of(FtpCrawl.class).build())
                .build();
        ftpCrawler.runSession("apache", ftpCrawlDefinition);
    }

    @After
    public void cleanup() throws InterruptedException {
        if (executorService != null) {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);
            executorService = null;
        }
    }

    public static class FtpCrawl extends FtpCrawlScriptEvent {

        @Override
        protected boolean run(final FtpCrawlSession session, final FtpCurrentCrawl crawl,
                              final Map<String, ?> attributes) {
            assertThat(session.getAttribute("attributeTest", Object.class), equalTo(attributeTest));
            return true;
        }
    }
}
