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

import com.qwazr.scripts.ScriptInterface;
import org.junit.Assert;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class CommonEvent {

	private final static Logger LOGGER = Logger.getLogger(CommonEvent.class.getName());

	public static class SessionEvent implements ScriptInterface {

		private final EventEnum eventEnum;
		private final Map<EventEnum, AtomicInteger> counters;

		protected SessionEvent(EventEnum eventEnum, Map<EventEnum, AtomicInteger> counters) {
			this.eventEnum = eventEnum;
			this.counters = counters;
		}

		@Override
		public void run(final Map<String, ?> map) throws Exception {
			counters.computeIfAbsent(eventEnum, (key) -> new AtomicInteger()).incrementAndGet();
			final CrawlSession crawlSession = (CrawlSession) map.get("session");
			Assert.assertNotNull(crawlSession);
			LOGGER.info(eventEnum.name());
		}
	}

	public static class CrawlEvent<T extends CurrentCrawl> extends SessionEvent {

		private final Class<T> currentClassClass;

		protected CrawlEvent(EventEnum eventEnum, Map<EventEnum, AtomicInteger> counters, Class<T> currentClassClass) {
			super(eventEnum, counters);
			this.currentClassClass = currentClassClass;
		}

		protected void checkCurrent(T current) {
			Assert.assertNotNull(current);
			Assert.assertEquals(currentClassClass, current.getClass());
			Assert.assertEquals(null, current.getError());
		}

		@Override
		public void run(final Map<String, ?> map) throws Exception {
			super.run(map);
			checkCurrent((T) map.get("current"));
		}
	}

}
