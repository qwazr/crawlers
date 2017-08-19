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
package com.qwazr.crawler.web;

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

public class WebCrawlerManager extends CrawlManager<WebCrawlThread, WebCrawlDefinition, WebCrawlStatus> {

	private static final Logger LOGGER = LoggerUtils.getLogger(WebCrawlerManager.class);

	private final WebCrawlerServiceInterface service;

	public WebCrawlerManager(final String myAddress, final ScriptManager scriptManager,
			final ExecutorService executor) {
		super(myAddress, scriptManager, executor, LOGGER);
		service = new WebCrawlerServiceImpl(this);
	}

	public WebCrawlerManager(ClusterManager clusterManager, ScriptManager scriptManager,
			ExecutorService executorService) {
		this(clusterManager.getServiceBuilder().local().getStatus().me, scriptManager, executorService);
	}

	public WebCrawlerServiceInterface getService() {
		return service;
	}

	public WebCrawlerManager registerContextAttribute(final GenericServer.Builder builder) {
		builder.contextAttribute(this);
		return this;
	}

	public WebCrawlerManager registerWebService(final ApplicationBuilder builder) {
		builder.singletons(service);
		return this;
	}

	@Override
	protected WebCrawlThread newCrawlThread(String sessionName, WebCrawlDefinition webCrawlDefinition) {
		final TimeTracker timeTracker = new TimeTracker();
		final WebCrawlStatus.Builder crawlStatusBuilder = WebCrawlStatus.of(myAddress, timeTracker, webCrawlDefinition);
		final CrawlSessionImpl<WebCrawlDefinition, WebCrawlStatus> session =
				new CrawlSessionImpl<>(sessionName, timeTracker, webCrawlDefinition, crawlStatusBuilder);
		return new WebCrawlThread(this, session, webCrawlDefinition);
	}

}
