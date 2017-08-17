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

import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.utils.WaitFor;
import org.junit.Assert;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class CommonEvent {

	public static CrawlStatus crawlWait(final String sessionName, final CrawlerServiceInterface service)
			throws InterruptedException {
		final AtomicReference<CrawlStatus> statusRef = new AtomicReference<>();
		WaitFor.of().timeOut(TimeUnit.MINUTES, 2).until(() -> {
			final CrawlStatus status = ErrorWrapper.bypass(() -> service.getSession(sessionName), 404);
			statusRef.set(status);
			return status != null && status.endTime != null;
		});
		return statusRef.get();
	}

	private final static Logger LOGGER = Logger.getLogger(CommonEvent.class.getName());

	public static class SessionEvent<T extends CurrentCrawl> extends CrawlScriptEvents<T> {

		private final EventEnum eventEnum;
		private final Map<EventEnum, AtomicInteger> counters;

		protected SessionEvent(EventEnum eventEnum, Map<EventEnum, AtomicInteger> counters) {
			this.eventEnum = eventEnum;
			this.counters = counters;
		}

		@Override
		protected void run(final CrawlSession crawlSession, final T currentCrawl, final Map<String, ?> attributes)
				throws Exception {
			counters.computeIfAbsent(eventEnum, (key) -> new AtomicInteger()).incrementAndGet();
			Assert.assertNotNull(crawlSession);
			LOGGER.info(eventEnum.name());
		}
	}

	public static class CrawlEvent<T extends CurrentCrawl> extends SessionEvent<T> {

		private final Class<T> currentClassClass;

		protected CrawlEvent(EventEnum eventEnum, Map<EventEnum, AtomicInteger> counters, Class<T> currentClassClass) {
			super(eventEnum, counters);
			this.currentClassClass = currentClassClass;
		}

		@Override
		protected void run(final CrawlSession crawlSession, final T currentCrawl, final Map<String, ?> attributes)
				throws Exception {
			super.run(crawlSession, currentCrawl, attributes);
			Assert.assertNotNull(currentCrawl);
			Assert.assertEquals(currentClassClass, currentCrawl.getClass());
			Assert.assertEquals(null, currentCrawl.getError());
		}

	}

}
