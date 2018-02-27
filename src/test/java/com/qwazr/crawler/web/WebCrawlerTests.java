package com.qwazr.crawler.web;

import com.qwazr.crawler.CrawlerServer;
import com.qwazr.server.RemoteService;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ WebCrawlerTests.Local.class, WebCrawlerTests.Remote.class })
public class WebCrawlerTests {

	private final static String REMOTE_URL = "http://localhost:9091";

	public static class Local extends WebCrawlerTestAbstract {

		@BeforeClass
		public static void setup() throws Exception {
			WebCrawlerTestAbstract.setup();
			service = CrawlerServer.getInstance().getWebCrawlerService();
		}
	}

	public static class Remote extends WebCrawlerTestAbstract {

		@BeforeClass
		public static void setup() throws Exception {
			WebCrawlerTestAbstract.setup();
			service = new WebCrawlerSingleClient(RemoteService.of(REMOTE_URL).build());
		}
	}

	public static class Remotes extends WebCrawlerTestAbstract {

		@BeforeClass
		public static void setup() throws Exception {
			WebCrawlerTestAbstract.setup();
			service = new WebCrawlerMultiClient(CrawlerServer.getInstance().getExecutorService(),
					RemoteService.of(REMOTE_URL).build());
		}
	}

}
