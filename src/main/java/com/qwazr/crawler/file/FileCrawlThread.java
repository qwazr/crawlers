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
 **/
package com.qwazr.crawler.file;

import com.qwazr.crawler.common.CrawlSessionImpl;
import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.crawler.common.CrawlThread;
import org.slf4j.Logger;

public class FileCrawlThread extends CrawlThread<FileCrawlerManager> {

	private final FileCrawlDefinition crawlDefinition;

	public FileCrawlThread(FileCrawlerManager manager, CrawlSessionImpl<FileCrawlDefinition> session, Logger logger) {
		super(manager, session, logger);
		crawlDefinition = session.getCrawlDefinition();
	}

	@Override
	protected void runner() throws Exception {

	}

	@Override
	public CrawlStatus getStatus() {
		return new CrawlStatus(manager.myAddress, crawlDefinition.entry_path, session);
	}
}