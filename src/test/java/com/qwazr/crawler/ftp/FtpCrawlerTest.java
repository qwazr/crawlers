package com.qwazr.crawler.ftp;

import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.common.ScriptDefinition;
import com.qwazr.scripts.ScriptManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
            System.out.println(crawl.getPath());
            assertThat(session.getAttribute("attributeTest", Object.class), equalTo(attributeTest));
            return true;
        }
    }
}
