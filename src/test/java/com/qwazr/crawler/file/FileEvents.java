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
import com.qwazr.crawler.common.CrawlSession;
import com.qwazr.crawler.common.EventEnum;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class FileEvents {

	public final static Map<EventEnum, CommonEvent.Feedback<CurrentPath>> feedbacks = new HashMap<>();

	public static class AfterCrawl extends CommonEvent.CrawlEvent<CurrentPath> {

		public AfterCrawl() {
			super(EventEnum.after_crawl, FileEvents.feedbacks, CurrentPath.class, CurrentPath::getPathString);
		}

		@Override
		protected boolean run(final CrawlSession crawlSession, final CurrentPath currentCrawl,
				final Map<String, ?> attributes) throws Exception {
			super.run(crawlSession, currentCrawl, attributes);
			Assert.assertNotNull(currentCrawl.path);
			Assert.assertNotNull(currentCrawl.attributes);
			return true;
		}

	}

	public static class BeforeCrawl extends CommonEvent.CrawlEvent<CurrentPath> {

		public BeforeCrawl() {
			super(EventEnum.before_crawl, FileEvents.feedbacks, CurrentPath.class, CurrentPath::getPathString);
		}

	}

	public static class BeforeSession extends CommonEvent.SessionEvent<CurrentPath> {

		public BeforeSession() {
			super(EventEnum.before_session, FileEvents.feedbacks);
		}

	}

	public static class AfterSession extends CommonEvent.SessionEvent<CurrentPath> {

		public AfterSession() {
			super(EventEnum.after_session, FileEvents.feedbacks);
		}
	}
}
