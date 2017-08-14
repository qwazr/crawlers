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
import com.qwazr.scripts.ScriptManager;
import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.GenericServer;
import com.qwazr.utils.LoggerUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class WebCrawlerManager extends CrawlManager<WebCrawlThread, WebCrawlDefinition> {

	private static final Logger LOGGER = LoggerUtils.getLogger(WebCrawlerManager.class);

	private final WebCrawlerServiceInterface service;

	public WebCrawlerManager(final ClusterManager clusterManager, final ScriptManager scriptManager,
			final ExecutorService executor) throws IOException, URISyntaxException {
		super(clusterManager, scriptManager, executor, LOGGER);
		service = new WebCrawlerServiceImpl(this);
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
	protected WebCrawlThread newCrawlThread(String sessionName, WebCrawlDefinition crawlDef) {
		return new WebCrawlThread(this, sessionName, crawlDef);
	}

}
