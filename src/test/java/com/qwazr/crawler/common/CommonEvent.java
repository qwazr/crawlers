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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
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

		protected final EventEnum eventEnum;
		protected final Map<EventEnum, Feedback<T>> feedbacks;

		protected SessionEvent(EventEnum eventEnum, Map<EventEnum, Feedback<T>> feedbacks) {
			this.eventEnum = eventEnum;
			this.feedbacks = feedbacks;
		}

		@Override
		protected void run(final CrawlSession crawlSession, final T currentCrawl, final Map<String, ?> attributes)
				throws Exception {
			feedbacks.computeIfAbsent(eventEnum, f -> new Feedback<>()).inform(attributes);
			Assert.assertNotNull(crawlSession);
			LOGGER.info(eventEnum.name());
		}
	}

	public static class CrawlEvent<T extends CurrentCrawl> extends SessionEvent<T> {

		private final Class<T> currentClassClass;
		private final Function<T, String> currentIdProvider;

		protected CrawlEvent(EventEnum eventEnum, Map<EventEnum, Feedback<T>> feedbacks, Class<T> currentClassClass,
				Function<T, String> currentIdProvider) {
			super(eventEnum, feedbacks);
			this.currentClassClass = currentClassClass;
			this.currentIdProvider = currentIdProvider;
		}

		@Override
		protected void run(final CrawlSession crawlSession, final T currentCrawl, final Map<String, ?> attributes)
				throws Exception {
			super.run(crawlSession, currentCrawl, attributes);
			feedbacks.computeIfAbsent(eventEnum, f -> new Feedback<>())
					.current(currentCrawl, currentIdProvider.apply(currentCrawl));
			Assert.assertNotNull(currentCrawl);
			Assert.assertEquals(currentClassClass, currentCrawl.getClass());
			Assert.assertEquals(null, currentCrawl.getError());
		}

	}

	public static class Feedback<T extends CurrentCrawl> {

		public final AtomicInteger counters;
		public final Map<String, Object> runAttributes;
		public final Map<String, T> currentCrawls;

		Feedback() {
			counters = new AtomicInteger();
			runAttributes = new HashMap<>();
			currentCrawls = new HashMap<>();
		}

		void inform(Map<String, ?> attributes) {
			counters.incrementAndGet();
			if (attributes != null)
				runAttributes.putAll(attributes);
		}

		void current(T currentCrawl, String currentId) {
			if (currentCrawl != null)
				currentCrawls.put(currentId, currentCrawl);
		}

		public int count() {
			return counters.get();
		}

		public Object attribute(String key) {
			return runAttributes.get(key);
		}

		public Integer crawlDepth(String id) {
			return currentCrawls.get(id).getDepth();
		}
	}

}
