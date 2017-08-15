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
package com.qwazr.crawler;

import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.crawler.common.CrawlerServiceInterface;
import com.qwazr.crawler.file.FileCrawlerTest;
import com.qwazr.crawler.web.WebCrawlerTest;
import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.utils.WaitFor;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(Suite.class)
@Suite.SuiteClasses({ WebCrawlerTest.class, FileCrawlerTest.class })
public class ServerTest {

	@AfterClass
	public static void cleanup() {
		CrawlerServer.shutdown();
	}

	public static void checkStarted() throws Exception {
		if (CrawlerServer.getInstance() == null)
			CrawlerServer.main();
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
}
