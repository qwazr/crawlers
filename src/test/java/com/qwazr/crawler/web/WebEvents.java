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
package com.qwazr.crawler.web;

import com.qwazr.crawler.common.CommonEvent;
import com.qwazr.crawler.common.EventEnum;

import java.util.HashMap;
import java.util.Map;

public class WebEvents {

	public final static Map<EventEnum, CommonEvent.Feedback<WebCrawlDefinition, WebCurrentCrawl>> feedbacks =
			new HashMap<>();

	public static class AfterCrawl extends WebCrawlScriptEvent {

		private final CommonEvent.CrawlCollector<WebCrawlDefinition, WebCrawlSession, WebCurrentCrawl> collector;

		public AfterCrawl() {
			collector = new CommonEvent.CrawlCollector<>(EventEnum.after_crawl, feedbacks,
					currentURI -> currentURI.getUri().toString());
		}

		@Override
		protected boolean run(final WebCrawlSession crawlSession, final WebCurrentCrawl currentCrawl,
				final Map<String, ?> attributes) {
			collector.collect(crawlSession, currentCrawl, attributes);
			assert !currentCrawl.isCrawled() || currentCrawl.getBody() != null;
			return true;
		}
	}

	public static class BeforeCrawl extends WebCrawlScriptEvent {

		private final CommonEvent.CrawlCollector<WebCrawlDefinition, WebCrawlSession, WebCurrentCrawl> collector;

		public BeforeCrawl() {
			collector = new CommonEvent.CrawlCollector<>(EventEnum.before_crawl, feedbacks,
					currentURI -> currentURI.getUri().toString());
		}

		@Override
		protected boolean run(final WebCrawlSession crawlSession, final WebCurrentCrawl currentCrawl,
				final Map<String, ?> attributes) {
			collector.collect(crawlSession, currentCrawl, attributes);
			assert !currentCrawl.isCrawled() || currentCrawl.getBody() != null;
			return true;
		}
	}

	public static class BeforeSession extends WebCrawlScriptEvent {

		private final CommonEvent.SessionCollector<WebCrawlDefinition, WebCrawlSession, WebCurrentCrawl> collector;

		public BeforeSession() {
			collector = new CommonEvent.SessionCollector<>(EventEnum.before_session, feedbacks);
		}

		@Override
		protected boolean run(final WebCrawlSession crawlSession, final WebCurrentCrawl currentCrawl,
				final Map<String, ?> attributes) {
			collector.collect(crawlSession, currentCrawl, attributes);
			return true;
		}

	}

	public static class AfterSession extends WebCrawlScriptEvent {

		private final CommonEvent.SessionCollector<WebCrawlDefinition, WebCrawlSession, WebCurrentCrawl> collector;

		public AfterSession() {
			collector = new CommonEvent.SessionCollector<>(EventEnum.after_session, feedbacks);
		}

		@Override
		protected boolean run(final WebCrawlSession crawlSession, final WebCurrentCrawl currentCrawl,
				final Map<String, ?> attributes) {
			collector.collect(crawlSession, currentCrawl, attributes);
			return true;
		}
	}
}
