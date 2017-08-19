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
import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.GenericServer;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.TimeTracker;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class FileCrawlerManager extends CrawlManager<FileCrawlThread, FileCrawlDefinition, FileCrawlStatus> {

	private static final Logger LOGGER = LoggerUtils.getLogger(FileCrawlerManager.class);
	private final FileCrawlerServiceInterface service;

	public FileCrawlerManager(String myAddress, ScriptManager scriptManager, ExecutorService executorService) {
		super(myAddress, scriptManager, executorService, LOGGER);
		service = new FileCrawlerServiceImpl(this);
	}

	public FileCrawlerManager(ClusterManager clusterManager, ScriptManager scriptManager,
			ExecutorService executorService) {
		this(clusterManager.getServiceBuilder().local().getStatus().me, scriptManager, executorService);
	}

	public FileCrawlerManager(ScriptManager scriptManager, ExecutorService executorService) {
		this((String) null, scriptManager, executorService);
	}

	public FileCrawlerServiceInterface getService() {
		return service;
	}

	public FileCrawlerManager registerContextAttribute(final GenericServer.Builder builder) {
		builder.contextAttribute(this);
		return this;
	}

	public FileCrawlerManager registerWebService(final ApplicationBuilder builder) {
		builder.singletons(service);
		return this;
	}

	@Override
	protected FileCrawlThread newCrawlThread(String sessionName, FileCrawlDefinition crawlDefinition) {
		final TimeTracker timeTracker = new TimeTracker();
		final FileCrawlStatus.Builder crawlStatusBuilder = FileCrawlStatus.of(myAddress, timeTracker, crawlDefinition);
		final CrawlSessionImpl<FileCrawlDefinition, FileCrawlStatus> session =
				new CrawlSessionImpl<>(sessionName, timeTracker, crawlDefinition, crawlStatusBuilder);
		return new FileCrawlThread(this, session, LOGGER);
	}

}
