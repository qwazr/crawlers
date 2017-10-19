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
package com.qwazr.crawler.web;

import com.qwazr.crawler.CrawlerServer;
import com.qwazr.crawler.common.CommonEvent;
import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.common.ScriptDefinition;
import com.qwazr.crawler.web.driver.DriverInterface;
import com.qwazr.utils.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.SortedMap;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class WebCrawlerTestAbstract {

	static WebCrawlerServiceInterface service;

	public static void setup() throws Exception {
		if (CrawlerServer.getInstance() == null)
			CrawlerServer.main();
		WebAppTestServer.start();
	}

	@AfterClass
	public static void cleanup() throws InterruptedException {
		WebAppTestServer.stop();
		CrawlerServer.shutdown();
	}

	@Test
	public void test200emptySessions() {
		SortedMap<String, WebCrawlStatus> sessions = service.getSessions();
		Assert.assertNotNull(sessions);
		Assert.assertTrue(sessions.isEmpty());
	}

	private WebCrawlDefinition.Builder getNewWebCrawl() {
		final WebCrawlDefinition.Builder webCrawl = WebCrawlDefinition.of();
		webCrawl.setTimeOutSecs(60);
		webCrawl.setMaxUrlNumber(10);
		webCrawl.addAcceptedContentType("text/html");
		webCrawl.addAcceptedContentType("image/vnd.microsoft.icon");
		webCrawl.setRobotsTxtEnabled(true);
		webCrawl.setMaxDepth(2);
		return webCrawl;
	}

	private CrawlStatus crawlTest(WebCrawlDefinition webCrawl, int crawled, int ignored, int error)
			throws InterruptedException {
		final String sessionName = RandomUtils.alphanumeric(10);
		service.runSession(sessionName, webCrawl);
		final CrawlStatus status = CommonEvent.crawlWait(sessionName, service);
		Assert.assertEquals(crawled, status.crawled);
		Assert.assertEquals(ignored, status.ignored);
		Assert.assertEquals(error, status.error);
		return status;
	}

	@Test
	public void test300SimpleCrawl() throws InterruptedException {
		crawlTest(getNewWebCrawl().setEntryUrl(WebAppTestServer.URL).build(), 4, 1, 1);
	}

	@Test
	public void test350CrawlGetWebRequest() throws InterruptedException {
		crawlTest(getNewWebCrawl().setEntryRequest(
				WebRequestDefinition.of(WebAppTestServer.URL).httpMethod(WebRequestDefinition.HttpMethod.GET).build())
				.build(), 4, 1, 1);
	}

	@Test
	public void test360CrawlPostWebRequest() throws InterruptedException {
		crawlTest(getNewWebCrawl().setEntryRequest(
				WebRequestDefinition.of(WebAppTestServer.URL).httpMethod(WebRequestDefinition.HttpMethod.POST).build())
				.build(), 0, 0, 1);
	}

	@Test
	public void test400CrawlEvent() throws InterruptedException {
		WebEvents.feedbacks.clear();
		final String sessionName = RandomUtils.alphanumeric(10);
		final WebCrawlDefinition.Builder webCrawl = getNewWebCrawl().setEntryUrl(WebAppTestServer.URL);
		final String variableName = RandomUtils.alphanumeric(5);
		final String variableValue = RandomUtils.alphanumeric(6);
		webCrawl.script(EventEnum.crawl, ScriptDefinition.of(WebEvents.Crawl.class)
				.variable(variableName, variableValue + EventEnum.crawl.name())
				.build());
		webCrawl.script(EventEnum.before_session, ScriptDefinition.of(WebEvents.BeforeSession.class)
				.variable(variableName, variableValue + EventEnum.before_session.name())
				.build());
		webCrawl.script(EventEnum.after_session, ScriptDefinition.of(WebEvents.AfterSession.class)
				.variable(variableName, variableValue + EventEnum.after_session.name())
				.build());
		webCrawl.userAgent("QWAZR_BOT");
		service.runSession(sessionName, webCrawl.build());
		final CrawlStatus status = CommonEvent.crawlWait(sessionName, service);

		Assert.assertEquals(2, status.crawled);
		Assert.assertEquals(1, status.ignored);
		Assert.assertEquals(1, status.redirect);
		Assert.assertEquals(1, status.error);

		Assert.assertEquals(1, WebEvents.feedbacks.get(EventEnum.before_session).count());
		Assert.assertEquals(5, WebEvents.feedbacks.get(EventEnum.crawl).count());
		Assert.assertEquals(1, WebEvents.feedbacks.get(EventEnum.after_session).count());
		for (EventEnum eventEnum : EventEnum.values())
			Assert.assertEquals(variableValue + eventEnum.name(),
					WebEvents.feedbacks.get(eventEnum).attribute(variableName));
		WebEvents.feedbacks.get(EventEnum.crawl).currentCrawls.forEach((id, current) -> {
			final DriverInterface.Body body = current.getBody();
			if (current.isCrawled()) {
				Assert.assertNotNull(body);
				Assert.assertEquals(Integer.valueOf(200), current.getStatusCode());
				Assert.assertNotNull(current.getContentType());
				Assert.assertNotNull(body.getContentType());
				Assert.assertEquals(current.getContentType(), body.getContentType());
				Assert.assertEquals(current.getContentType(), body.getContent().getContentType());
				Assert.assertNotNull(body.getContent().getContentLength());
				Assert.assertTrue(body.getContent().isClosed());
			}
			if (current.getRedirect() != null) {
				Assert.assertEquals(Integer.valueOf(302), current.getStatusCode());
			}
			if (current.getError() != null) {
				Assert.assertEquals(Integer.valueOf(404), current.getStatusCode());
			}
			if (current.isIgnored()) {
				Assert.assertNull(current.getStatusCode());
				Assert.assertNull(current.getContentType());
			}
		});
	}

}
