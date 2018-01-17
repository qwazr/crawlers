/*
 * Copyright 2017-2018 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileCrawlManagerTest {

	private static ExecutorService executorService;
	private static FileCrawlerManager crawlerManager;

	@BeforeClass
	public static void setup() {
		executorService = Executors.newCachedThreadPool();
		crawlerManager = new FileCrawlerManager(null, executorService);
	}

	@AfterClass
	public static void cleanup() {
		if (executorService != null)
			executorService.shutdown();
	}

	FileCrawlDefinition.Builder getFileCrawlDefinition() {
		return FileCrawlDefinition.of()
				.entryPath("src/test/file_crawl")
				.addInclusionPattern("*/")
				.addInclusionPattern("*.txt")
				.addExclusionPattern("*/ignore.*")
				.addExclusionPattern("*/ignore/");
	}

	void crawlTest(FileCrawlDefinition.Builder fileCrawlDefinitionBuilder, int expectedCrawled, int expectedIgnored,
			int expectedError) throws InterruptedException {
		final String crawlSession = RandomUtils.alphanumeric(5);
		FileCrawlStatus crawlStatus = crawlerManager.runSession(crawlSession, fileCrawlDefinitionBuilder.build());
		Assert.assertNotNull(crawlStatus);
		while (crawlStatus.endTime == null) {
			crawlStatus = crawlerManager.getSession(crawlSession);
			Thread.sleep(500);
		}
		Assert.assertEquals(expectedCrawled, crawlStatus.crawled);
		Assert.assertEquals(expectedIgnored, crawlStatus.ignored);
		Assert.assertEquals(expectedError, crawlStatus.error);
		Assert.assertNotNull(crawlStatus.threadDone);

		if (expectedError == 0)
			Assert.assertNull(crawlStatus.lastError);
		else
			Assert.assertNotNull(crawlStatus.lastError);

	}

	@Test
	public void crawlFullTest() throws InterruptedException {
		crawlTest(getFileCrawlDefinition(), 7, 2, 0);
	}

	@Test
	public void crawlDepth() throws InterruptedException {
		crawlTest(getFileCrawlDefinition().setMaxDepth(1), 4, 1, 0);
		crawlTest(getFileCrawlDefinition().setMaxDepth(2), 6, 2, 0);
		crawlTest(getFileCrawlDefinition().setMaxDepth(0), 1, 0, 0);
	}
}
