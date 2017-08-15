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
package com.qwazr.crawler.web;

import com.qwazr.crawler.common.CommonEvent;
import com.qwazr.crawler.common.EventEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class WebEvents {

	public final static Map<EventEnum, AtomicInteger> counters = new HashMap<>();

	public static class BeforeCrawl extends CommonEvent.CrawlEvent<CurrentURIImpl> {

		public BeforeCrawl() {
			super(EventEnum.before_crawl, counters, CurrentURIImpl.class);
		}

	}

	public static class AfterCrawl extends CommonEvent.CrawlEvent<CurrentURIImpl> {

		public AfterCrawl() {
			super(EventEnum.after_crawl, counters, CurrentURIImpl.class);
		}
	}

	public static class BeforeSession extends CommonEvent.SessionEvent {

		public BeforeSession() {
			super(EventEnum.before_session, counters);
		}

	}

	public static class AfterSession extends CommonEvent.SessionEvent {

		public AfterSession() {
			super(EventEnum.after_session, counters);
		}
	}
}
