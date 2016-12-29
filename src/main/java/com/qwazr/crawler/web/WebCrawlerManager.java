/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web;

import com.qwazr.cluster.ClusterManager;
import com.qwazr.cluster.ClusterServiceInterface;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.server.GenericServer;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

public class WebCrawlerManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebCrawlerManager.class);

	final String myAddress;
	final ClusterServiceInterface clusterService;
	final ScriptManager scriptManager;
	private final ExecutorService executorService;

	private final LockUtils.ReadWriteLock rwlSessionMap = new LockUtils.ReadWriteLock();
	private final HashMap<String, WebCrawlThread> crawlSessionMap;

	private WebCrawlerServiceBuilder serviceBuilder;

	public WebCrawlerManager(final ClusterManager clusterManager, final ScriptManager scriptManager,
			final ExecutorService executor, final GenericServer.Builder builder)
			throws IOException, URISyntaxException {
		this.scriptManager = scriptManager;
		this.clusterService = clusterManager.getService();
		myAddress = clusterService.getStatus().me;
		this.executorService = executor;
		crawlSessionMap = new HashMap<>();
		if (builder != null) {
			builder.webService(WebCrawlerServiceImpl.class);
			builder.contextAttribute(this);
		}
		serviceBuilder = new WebCrawlerServiceBuilder(new WebCrawlerServiceImpl(this));
	}

	WebCrawlerServiceInterface getService() {
		return serviceBuilder.local;
	}

	WebCrawlerServiceBuilder getServiceBuilder() {
		return serviceBuilder;
	}

	TreeMap<String, WebCrawlStatus> getSessions() {
		return rwlSessionMap.read(() -> {
			final TreeMap<String, WebCrawlStatus> map = new TreeMap<>();
			for (Map.Entry<String, WebCrawlThread> entry : crawlSessionMap.entrySet())
				map.put(entry.getKey(), entry.getValue().getStatus());
			return map;
		});
	}

	WebCrawlStatus getSession(final String sessionName) {
		return rwlSessionMap.read(() -> {
			final WebCrawlThread crawlThread = crawlSessionMap.get(sessionName);
			if (crawlThread == null)
				return null;
			return crawlThread.getStatus();
		});
	}

	void abortSession(final String sessionName, final String abortingReason) throws ServerException {
		rwlSessionMap.readEx(() -> {
			final WebCrawlThread crawlThread = crawlSessionMap.get(sessionName);
			if (crawlThread == null)
				throw new ServerException(Status.NOT_FOUND, "Session not found: " + sessionName);
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Aborting session: " + sessionName + " - " + abortingReason);
			crawlThread.abort(abortingReason);
		});
	}

	WebCrawlStatus runSession(final String sessionName, final WebCrawlDefinition crawlJson) throws ServerException {
		return rwlSessionMap.writeEx(() -> {
			if (crawlSessionMap.containsKey(sessionName))
				throw new ServerException(Status.CONFLICT, "The session already exists: " + sessionName);
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Create session: " + sessionName);

			WebCrawlThread crawlThread = new WebCrawlThread(this, sessionName, crawlJson);
			crawlSessionMap.put(sessionName, crawlThread);
			executorService.execute(crawlThread);
			return crawlThread.getStatus();
		});
	}

	void removeSession(final WebCrawlThread crawlThread) {
		rwlSessionMap.writeEx(() -> {
			final String sessionName = crawlThread.getSessionName();
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Remove session: " + sessionName);
			crawlSessionMap.remove(sessionName, crawlThread);
		});
	}

}
