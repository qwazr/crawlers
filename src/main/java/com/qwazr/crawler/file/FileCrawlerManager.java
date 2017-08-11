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
package com.qwazr.crawler.file;

import com.qwazr.cluster.ClusterManager;
import com.qwazr.crawler.common.CrawlManager;
import com.qwazr.crawler.common.CrawlSessionImpl;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.utils.LoggerUtils;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class FileCrawlerManager extends CrawlManager<FileCrawlThread, FileCrawlDefinition> {

	private static final Logger LOGGER = LoggerUtils.getLogger(FileCrawlerManager.class);

	protected FileCrawlerManager(ClusterManager clusterManager, ScriptManager scriptManager,
			ExecutorService executorService) {
		super(clusterManager, scriptManager, executorService, LOGGER);
	}

	@Override
	protected FileCrawlThread newCrawlThread(String sessionName, FileCrawlDefinition crawlDefinition) {
		return new FileCrawlThread(this, new CrawlSessionImpl<>(crawlDefinition, sessionName), LOGGER);
	}
}
