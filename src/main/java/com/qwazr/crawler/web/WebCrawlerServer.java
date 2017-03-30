/**
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
import com.qwazr.library.LibraryManager;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.WelcomeShutdownService;
import com.qwazr.server.configuration.ServerConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebCrawlerServer implements BaseServer {

	private final GenericServer server;
	private final WebCrawlerServiceBuilder serviceBuilder;

	private WebCrawlerServer(final ServerConfiguration configuration) throws IOException, URISyntaxException {
		final ExecutorService executorService = Executors.newCachedThreadPool();
		final GenericServer.Builder builder =
				GenericServer.of(configuration, executorService).webService(WelcomeShutdownService.class);
		final ClusterManager clusterManager =
				new ClusterManager(executorService, configuration).registerHttpClientMonitoringThread(builder)
						.registerProtocolListener(builder)
						.registerWebService(builder);
		final LibraryManager libraryManager =
				new LibraryManager(null, configuration.dataDirectory, configuration.getEtcFiles()).registerWebService(
						builder);
		final ScriptManager scriptManager = new ScriptManager(executorService, clusterManager, libraryManager,
				configuration.dataDirectory).registerWebService(builder);
		final WebCrawlerManager webCrawlerManager =
				new WebCrawlerManager(clusterManager, scriptManager, executorService).registerWebService(builder);
		serviceBuilder = new WebCrawlerServiceBuilder(clusterManager, webCrawlerManager);
		server = builder.build();
	}

	@Override
	public GenericServer getServer() {
		return server;
	}

	public WebCrawlerServiceBuilder getServiceBuilder() {
		return serviceBuilder;
	}

	private static volatile WebCrawlerServer INSTANCE;

	public static WebCrawlerServer getInstance() {
		return INSTANCE;
	}

	public static synchronized void main(final String... args) throws Exception {
		shutdown();
		INSTANCE = new WebCrawlerServer(new ServerConfiguration(args));
		INSTANCE.start();
	}

	public static synchronized void shutdown() {
		if (INSTANCE == null)
			return;
		INSTANCE.stop();
		INSTANCE = null;
	}

}