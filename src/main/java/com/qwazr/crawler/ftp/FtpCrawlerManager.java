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
package com.qwazr.crawler.ftp;

import com.qwazr.cluster.ClusterManager;
import com.qwazr.crawler.common.CrawlManager;
import com.qwazr.crawler.common.CrawlSessionImpl;
import com.qwazr.crawler.file.FileCrawlerManager;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.TimeTracker;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class FtpCrawlerManager extends CrawlManager<FtpCrawlThread, FtpCrawlDefinition, FtpCrawlStatus> {

	private static final Logger LOGGER = LoggerUtils.getLogger(FileCrawlerManager.class);
	private final FtpCrawlerServiceInterface service;

	public FtpCrawlerManager(String myAddress, ScriptManager scriptManager, ExecutorService executorService) {
		super(myAddress, scriptManager, executorService, LOGGER);
		service = new FtpCrawlerServiceImpl(this);
	}

	public FtpCrawlerManager(ClusterManager clusterManager, ScriptManager scriptManager,
			ExecutorService executorService) {
		this(clusterManager.getService().getStatus().me, scriptManager, executorService);
	}

	public FtpCrawlerManager(ScriptManager scriptManager, ExecutorService executorService) {
		this((String) null, scriptManager, executorService);
	}

	public FtpCrawlerServiceInterface getService() {
		return service;
	}

	@Override
	protected FtpCrawlThread newCrawlThread(String sessionName, FtpCrawlDefinition crawlDefinition) {
		final TimeTracker timeTracker = new TimeTracker();
		final FtpCrawlStatus.Builder crawlStatusBuilder = FtpCrawlStatus.of(myAddress, timeTracker, crawlDefinition);
		final CrawlSessionImpl<FtpCrawlDefinition, FtpCrawlStatus> session =
				new CrawlSessionImpl<>(sessionName, timeTracker, crawlDefinition, crawlStatusBuilder);
		return new FtpCrawlThread(this, session, LOGGER);
	}
}