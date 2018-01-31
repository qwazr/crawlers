package com.qwazr.crawler.ftp;

import com.qwazr.crawler.common.CrawlScriptEvents;
import com.qwazr.crawler.common.CrawlSession;
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

public class FtpCrawlerTest {

	private FtpCrawlerServiceInterface ftpCrawler;
	private ExecutorService executorService;

	@Before
	public void setup() {
		executorService = Executors.newCachedThreadPool();
		ScriptManager scriptManager = new ScriptManager(executorService, Paths.get(".").toFile());
		FtpCrawlerManager ftpCrawlerManager = new FtpCrawlerManager(scriptManager, executorService);
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

	public static class FtpCrawl extends CrawlScriptEvents<CurrentFtpCrawl> {

		@Override
		protected boolean run(CrawlSession session, CurrentFtpCrawl crawl, Map<String, ?> attributes) throws Exception {
			System.out.println(crawl.getPath());
			return true;
		}
	}
}
