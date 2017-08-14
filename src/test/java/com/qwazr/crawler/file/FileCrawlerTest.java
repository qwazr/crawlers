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
package com.qwazr.crawler.file;

import com.qwazr.crawler.CrawlerServer;
import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.crawler.web.test.WebCrawlerTest;
import com.qwazr.server.RemoteService;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.TreeMap;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileCrawlerTest {

	private static FileCrawlerServiceInterface local;
	private static FileCrawlerServiceInterface remote;

	@Test
	public void test100startServer() throws Exception {
		CrawlerServer.main();
		local = CrawlerServer.getInstance().getFileServiceBuilder().local();
		Assert.assertNotNull(local);
		remote = new FileCrawlerServiceBuilder(null, null).remote(RemoteService.of("http://localhost:9091").build());
		Assert.assertNotNull(remote);
	}

	@Test
	public void test200emptySessions() {
		TreeMap<String, CrawlStatus> sessions = remote.getSessions(null);
		Assert.assertNotNull(sessions);
		Assert.assertTrue(sessions.isEmpty());
	}

	private FileCrawlDefinition.Builder getNewCrawl() {
		return FileCrawlDefinition.of()
				.entryPath("src/test/file_crawl")
				.addInclusionPattern("*.txt")
				.addExclusionPattern("*/ignore.*")
				.setCrawlWaitMs(10)
				.maxDepth(10);
	}

	@Test
	public void test300SimpleCrawl() throws InterruptedException {
		final String sessionName = RandomUtils.alphanumeric(10);
		remote.runSession(sessionName, getNewCrawl().build());
		final CrawlStatus status = WebCrawlerTest.crawlWait(sessionName, remote);
		Assert.assertEquals(2, status.crawled);
		Assert.assertEquals(1, status.ignored);
		Assert.assertEquals(0, status.error);
		Assert.assertNull(status.lastError);
	}

	@Test
	public void test900stopServer() throws Exception {
		CrawlerServer.shutdown();
		Assert.assertNull(CrawlerServer.getInstance());
	}
}
