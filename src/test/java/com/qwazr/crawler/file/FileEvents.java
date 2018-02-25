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

import com.qwazr.crawler.common.CommonEvent;
import com.qwazr.crawler.common.EventEnum;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class FileEvents {

	public final static Map<EventEnum, CommonEvent.Feedback<FileCrawlDefinition, FileCurrentPath>> feedbacks =
			new HashMap<>();

	public static class AfterCrawl extends FileCrawlScriptEvent {

		private final CommonEvent.CrawlCollector<FileCrawlDefinition, FileCrawlSession, FileCurrentPath> collector;

		public AfterCrawl() {
			collector =
					new CommonEvent.CrawlCollector<>(EventEnum.after_crawl, feedbacks, FileCurrentPath::getPathString);
		}

		@Override
		protected boolean run(final FileCrawlSession crawlSession, final FileCurrentPath currentCrawl,
				final Map<String, ?> attributes) {
			collector.collect(crawlSession, currentCrawl, attributes);
			Assert.assertNotNull(currentCrawl.path);
			Assert.assertNotNull(currentCrawl.attributes);
			return true;
		}

	}

	public static class BeforeCrawl extends FileCrawlScriptEvent {

		private final CommonEvent.CrawlCollector<FileCrawlDefinition, FileCrawlSession, FileCurrentPath> collector;

		public BeforeCrawl() {
			collector =
					new CommonEvent.CrawlCollector<>(EventEnum.before_crawl, feedbacks, FileCurrentPath::getPathString);
		}

		@Override
		protected boolean run(final FileCrawlSession crawlSession, final FileCurrentPath currentCrawl,
				final Map<String, ?> attributes) {
			collector.collect(crawlSession, currentCrawl, attributes);
			Assert.assertNotNull(currentCrawl.path);
			Assert.assertNotNull(currentCrawl.attributes);
			return true;
		}

	}

	public static class BeforeSession extends FileCrawlScriptEvent {

		private final CommonEvent.SessionCollector<FileCrawlDefinition, FileCrawlSession, FileCurrentPath> collector;

		public BeforeSession() {
			collector = new CommonEvent.SessionCollector<>(EventEnum.before_session, feedbacks);
		}

		@Override
		protected boolean run(FileCrawlSession session, FileCurrentPath crawl, final Map<String, ?> attributes) {
			collector.collect(session, crawl, attributes);
			return true;
		}
	}

	public static class AfterSession extends FileCrawlScriptEvent {

		private final CommonEvent.SessionCollector<FileCrawlDefinition, FileCrawlSession, FileCurrentPath> collector;

		public AfterSession() {
			collector = new CommonEvent.SessionCollector<>(EventEnum.after_session, feedbacks);
		}

		@Override
		protected boolean run(FileCrawlSession session, FileCurrentPath crawl, final Map<String, ?> attributes) {
			collector.collect(session, crawl, attributes);
			return true;
		}
	}
}
