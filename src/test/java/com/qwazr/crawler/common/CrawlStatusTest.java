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
package com.qwazr.crawler.common;

import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.RandomUtils;
import com.qwazr.utils.TimeTracker;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CrawlStatusTest {

	@Test
	public void checkBuilderAndGetter() throws IOException {
		final String entryCrawl = RandomUtils.alphanumeric(12);
		final String nodeAddress = RandomUtils.alphanumeric(10);
		final TimeTracker timeTracker = new TimeTracker();
		final String lastError = RandomUtils.alphanumeric(20);
		final String currentCrawl = RandomUtils.alphanumeric(16);
		final Integer currentDepth = RandomUtils.nextInt(1, 100);
		final String abortingReason = RandomUtils.alphanumeric(25);

		final CrawlStatus status = CrawlStatus.of(entryCrawl, nodeAddress, timeTracker)
				.incError()
				.lastError(lastError)
				.crawl(currentCrawl, currentDepth)
				.abort(abortingReason)
				.incCrawled()
				.incCrawled()
				.incCrawled()
				.incIgnored()
				.incIgnored()
				.done()
				.build();

		Assert.assertEquals(entryCrawl, status.getEntryCrawl());
		Assert.assertEquals(nodeAddress, status.getNodeAddress());
		Assert.assertEquals(timeTracker.getStatus(), status.getTimer());
		Assert.assertEquals(lastError, status.getLastError());
		Assert.assertEquals(currentCrawl, status.getCurrentCrawl());
		Assert.assertEquals(currentDepth, status.getCurrentDepth());
		Assert.assertEquals(abortingReason, status.getAbortingReason());
		Assert.assertEquals(true, status.getAborting());
		Assert.assertEquals(3, status.getCrawled());
		Assert.assertEquals(2, status.getIgnored());
		Assert.assertEquals(1, status.getError());
		Assert.assertNotNull(status.getStartTime());
		Assert.assertNotNull(status.getEndTime());

		final CrawlStatus status2 =
				ObjectMappers.JSON.readValue(ObjectMappers.JSON.writeValueAsString(status), CrawlStatus.class);
		Assert.assertEquals(status, status2);
	}
}
