/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web.test;

import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.crawler.common.CrawlerServiceInterface;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.common.ScriptDefinition;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.CrawlerServer;
import com.qwazr.crawler.web.WebCrawlerServiceBuilder;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.server.RemoteService;
import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.utils.RandomUtils;
import com.qwazr.utils.WaitFor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
		CrawlerServer.main();
		local = CrawlerServer.getInstance().getWebServiceBuilder().local();
		Assert.assertNotNull(local);
		remote = new WebCrawlerServiceBuilder(null, null).remote(RemoteService.of("http://localhost:9091").build());
		Assert.assertNotNull(remote);
	}

	@Test
	public void test200emptySessions() {
		TreeMap<String, CrawlStatus> sessions = remote.getSessions(null);
		Assert.assertNotNull(sessions);
		Assert.assertTrue(sessions.isEmpty());
	}

	private WebCrawlDefinition.Builder getNewWebCrawl() {
		final WebCrawlDefinition.Builder webCrawl = WebCrawlDefinition.of();
		webCrawl.setBrowserType("html_unit");
		webCrawl.setJavascriptEnabled(false);
		webCrawl.setImplicitlyWait(0);
		webCrawl.setDownloadImages(false);
		webCrawl.setMaxUrlNumber(10);
		webCrawl.setRobotsTxtEnabled(true);
		webCrawl.setMaxDepth(2);
		webCrawl.setEntryUrl(WebAppTestServer.URL);
		return webCrawl;
	}

	public static CrawlStatus crawlWait(final String sessionName, final CrawlerServiceInterface service)
			throws InterruptedException {
		final AtomicReference<CrawlStatus> statusRef = new AtomicReference<>();
		WaitFor.of().timeOut(TimeUnit.MINUTES, 2).until(() -> {
			final CrawlStatus status = ErrorWrapper.bypass(() -> service.getSession(sessionName, null), 404);
			statusRef.set(status);
			return status != null && status.endTime != null;
		});
		return statusRef.get();
	}

	@Test
	public void test300SimpleCrawl() throws InterruptedException {
		final String sessionName = RandomUtils.alphanumeric(10);
		remote.runSession(sessionName, getNewWebCrawl().build());
		crawlWait(sessionName, remote);
	}

	@Test
	public void test400CrawlEvent() throws InterruptedException {
		final String sessionName = RandomUtils.alphanumeric(10);
		final WebCrawlDefinition.Builder webCrawl = getNewWebCrawl();
		webCrawl.script(EventEnum.before_crawl, ScriptDefinition.of().name(BeforeCrawl.class.getName()).build());
		webCrawl.script(EventEnum.after_crawl, ScriptDefinition.of().name(AfterCrawl.class.getName()).build());
		remote.runSession(sessionName, webCrawl.build());
		crawlWait(sessionName, remote);
		Assert.assertEquals(5, BeforeCrawl.count.get());
		Assert.assertEquals(4, AfterCrawl.count.get());
	}

	@Test
	public void test900stopServer() throws Exception {
		CrawlerServer.shutdown();
		Assert.assertNull(CrawlerServer.getInstance());
	}

}
