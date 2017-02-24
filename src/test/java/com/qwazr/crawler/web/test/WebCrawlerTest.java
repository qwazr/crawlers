/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
 **/
package com.qwazr.crawler.web.test;

import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebCrawlStatus;
import com.qwazr.crawler.web.WebCrawlerServer;
import com.qwazr.crawler.web.WebCrawlerServiceBuilder;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.server.RemoteService;
import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.utils.WaitFor;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.TreeMap;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebCrawlerTest {

	private static WebCrawlerServiceInterface local;
	private static WebCrawlerServiceInterface remote;

	@BeforeClass
	public static void before() throws Exception {
		WebAppTestServer.start();
	}

	@AfterClass
	public static void after() throws InterruptedException {
		WebAppTestServer.stop();
	}

	@Test
	public void test100startServer() throws Exception {
		WebCrawlerServer.main();
		local = WebCrawlerServer.getInstance().getServiceBuilder().local();
		Assert.assertNotNull(local);
		remote = new WebCrawlerServiceBuilder(null, null).remote(RemoteService.of("http://localhost:9091").build());
		Assert.assertNotNull(remote);
	}

	@Test
	public void test200emptySessions() {
		TreeMap<String, WebCrawlStatus> sessions = remote.getSessions(null);
		Assert.assertNotNull(sessions);
		Assert.assertTrue(sessions.isEmpty());
	}

	private WebCrawlDefinition getNewWebCrawl() {
		final WebCrawlDefinition webCrawl = new WebCrawlDefinition();
		webCrawl.setBrowserType("html_unit");
		webCrawl.setJavascriptEnabled(false);
		webCrawl.setImplicitlyWait(0);
		webCrawl.setDownloadImages(false);
		webCrawl.entry_url = WebAppTestServer.URL;
		return webCrawl;
	}

	private void crawlWait(final String sessionName, final int crawlCount) throws InterruptedException {
		WaitFor.of().timeOut(TimeUnit.MINUTES, 2).until(() -> {
			WebCrawlStatus status = ErrorWrapper.bypass(() -> remote.getSession(sessionName, null), 404);
			if (status == null)
				return false;
			return status.urls.crawled == crawlCount;
		});
	}

	@Test
	public void test300SimpleCrawl() throws InterruptedException {
		final String sessionName = RandomStringUtils.randomAlphanumeric(10);
		remote.runSession(sessionName, getNewWebCrawl());
		crawlWait(sessionName, 1);
	}

	@Test
	public void test400CrawlEvent() throws InterruptedException {
		final String sessionName = RandomStringUtils.randomAlphanumeric(10);
		final WebCrawlDefinition webCrawl = getNewWebCrawl();
		webCrawl.scripts = new HashMap<>();
		webCrawl.scripts.put(WebCrawlDefinition.EventEnum.before_crawl,
				new WebCrawlDefinition.Script(BeforeCrawl.class.getName()));
		remote.runSession(sessionName, webCrawl);
		crawlWait(sessionName, 1);
		Assert.assertEquals(2, BeforeCrawl.count.get());
	}

	@Test
	public void test900stopServer() throws Exception {
		WebCrawlerServer.shutdown();
		Assert.assertNull(WebCrawlerServer.getInstance());
	}

}
