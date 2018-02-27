/*
 * Copyright 2016-2018 Emmanuel Keller / QWAZR
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
import com.qwazr.cluster.ServiceBuilderAbstract;
import com.qwazr.server.RemoteService;

import java.util.concurrent.ExecutorService;

public class WebCrawlerServiceBuilder extends ServiceBuilderAbstract<WebCrawlerServiceInterface> {

	private final ExecutorService executorService;

	public WebCrawlerServiceBuilder(final ExecutorService executorService, final ClusterManager clusterManager,
			final WebCrawlerManager webCrawlerManager) {
		super(clusterManager, WebCrawlerServiceInterface.SERVICE_NAME,
				webCrawlerManager == null ? null : webCrawlerManager.getService());
		this.executorService = executorService;
	}

	@Override
	final public WebCrawlerServiceInterface remote(final RemoteService remote) {
		return new WebCrawlerSingleClient(remote);
	}

	@Override
	final public WebCrawlerServiceInterface remotes(final RemoteService... remotes) {
		return new WebCrawlerMultiClient(executorService, remotes);
	}
}
