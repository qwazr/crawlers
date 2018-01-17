package com.qwazr.crawler.ftp;

import com.qwazr.crawler.common.CrawlScriptEvents;
import com.qwazr.crawler.common.CrawlSession;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.common.ScriptDefinition;
import com.qwazr.scripts.ScriptManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

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

	@Ignore
	public void test() {
		FtpCrawlDefinition ftpCrawlDefinition = FtpCrawlDefinition.of()
				.hostname("echanges.dila.gouv.fr")
				.entryPath("/BOAMP/")
				.passive(true)
				.username("anonymous")
				.password("contact@qwazr.com")
				.addInclusionPattern("BOAMP")
				.addInclusionPattern("BOAMP/2018/*")
				.addInclusionPattern("BOAMP/2017/*")
				.addInclusionPattern("*.taz")
				.script(EventEnum.after_crawl, ScriptDefinition.of(FtpCrawl.class).build())
				.build();
		ftpCrawler.runSession("boamp", ftpCrawlDefinition);
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
