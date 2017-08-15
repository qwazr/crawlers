/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.crawler.common.CrawlerServiceImpl;
import com.qwazr.utils.LoggerUtils;

import java.io.IOException;
import java.util.logging.Logger;

class FileCrawlerServiceImpl extends CrawlerServiceImpl<FileCrawlerManager, FileCrawlDefinition>
		implements FileCrawlerServiceInterface {

	private static final Logger LOGGER = LoggerUtils.getLogger(FileCrawlerServiceImpl.class);

	FileCrawlerServiceImpl(FileCrawlerManager crawlerManager) {
		super(LOGGER, crawlerManager);
	}

	public CrawlStatus runSession(final String session_name, final String jsonCrawlDefinition) throws IOException {
		return runSession(session_name, FileCrawlDefinition.newInstance(jsonCrawlDefinition));
	}

}