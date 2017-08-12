/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import com.qwazr.crawler.common.CrawlStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileCrawlManagerTest {

	private static ExecutorService executorService;
	private static FileCrawlerManager fileCrawlerManager;

	@BeforeClass
	public static void setup() {
		executorService = Executors.newCachedThreadPool();
		fileCrawlerManager = new FileCrawlerManager(null, null, executorService);
	}

	@AfterClass
	public static void cleanup() {
		if (executorService != null)
			executorService.shutdown();
	}

	@Test
	public void crawlFullTest() throws InterruptedException {
		CrawlStatus crawlStatus = fileCrawlerManager.runSession("myFileCrawlSession",
				FileCrawlDefinition.of().entryPath("src/test/file_crawl").build());
		Assert.assertNotNull(crawlStatus);
		while (crawlStatus.endTime != null) {
			crawlStatus = fileCrawlerManager.getSession("myFileCrawlSession");
			Thread.sleep(500);
		}
	}
}
