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
package com.qwazr.crawler.web.manager;

import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.crawler.web.client.WebCrawlerMultiClient;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.crawler.web.service.WebCrawlStatus;
import com.qwazr.crawler.web.service.WebCrawlerServiceImpl;
import com.qwazr.utils.LockUtils;
import com.qwazr.server.RemoteService;
import com.qwazr.server.ServerBuilder;
import com.qwazr.server.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

public class WebCrawlerManager {

	public final static String SERVICE_NAME_WEBCRAWLER = "webcrawler";

	private static final Logger logger = LoggerFactory.getLogger(WebCrawlerManager.class);

	static WebCrawlerManager INSTANCE = null;

	private final ExecutorService executorService;

	public synchronized static void load(final ExecutorService executorService, final ServerBuilder builder)
			throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		try {
			INSTANCE = new WebCrawlerManager(executorService);
			if (builder != null)
				builder.registerWebService(WebCrawlerServiceImpl.class);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public static WebCrawlerManager getInstance() {
		if (WebCrawlerManager.INSTANCE == null)
			throw new RuntimeException("The web crawler service is not enabled");
		return WebCrawlerManager.INSTANCE;
	}

	private final LockUtils.ReadWriteLock rwlSessionMap = new LockUtils.ReadWriteLock();
	private final HashMap<String, WebCrawlThread> crawlSessionMap;

	private WebCrawlerManager(final ExecutorService executor) throws IOException, URISyntaxException {
		this.executorService = executor;
		crawlSessionMap = new HashMap<>();
	}

	public TreeMap<String, WebCrawlStatus> getSessions() {
		return rwlSessionMap.read(() -> {
			final TreeMap<String, WebCrawlStatus> map = new TreeMap<String, WebCrawlStatus>();
			for (Map.Entry<String, WebCrawlThread> entry : crawlSessionMap.entrySet())
				map.put(entry.getKey(), entry.getValue().getStatus());
			return map;
		});
	}

	public WebCrawlStatus getSession(final String sessionName) {
		return rwlSessionMap.read(() -> {
			final WebCrawlThread crawlThread = crawlSessionMap.get(sessionName);
			if (crawlThread == null)
				return null;
			return crawlThread.getStatus();
		});
	}

	public void abortSession(final String sessionName, final String abortingReason) throws ServerException {
		rwlSessionMap.readEx(() -> {
			final WebCrawlThread crawlThread = crawlSessionMap.get(sessionName);
			if (crawlThread == null)
				throw new ServerException(Status.NOT_FOUND, "Session not found: " + sessionName);
			if (logger.isInfoEnabled())
				logger.info("Aborting session: " + sessionName + " - " + abortingReason);
			crawlThread.abort(abortingReason);
		});
	}

	public WebCrawlStatus runSession(final String sessionName, final WebCrawlDefinition crawlJson)
			throws ServerException {
		return rwlSessionMap.writeEx(() -> {
			if (crawlSessionMap.containsKey(sessionName))
				throw new ServerException(Status.CONFLICT, "The session already exists: " + sessionName);
			if (logger.isInfoEnabled())
				logger.info("Create session: " + sessionName);

			WebCrawlThread crawlThread = new WebCrawlThread(sessionName, crawlJson);
			crawlSessionMap.put(sessionName, crawlThread);
			executorService.execute(crawlThread);
			return crawlThread.getStatus();
		});
	}

	void removeSession(final WebCrawlThread crawlThread) {
		rwlSessionMap.writeEx(() -> {
			final String sessionName = crawlThread.getSessionName();
			if (logger.isInfoEnabled())
				logger.info("Remove session: " + sessionName);
			crawlSessionMap.remove(sessionName, crawlThread);
		});
	}

	public WebCrawlerMultiClient getMultiClient(final String group) throws URISyntaxException {
		SortedSet<String> urls = ClusterManager.INSTANCE.getNodesByGroupByService(group, SERVICE_NAME_WEBCRAWLER);
		return new WebCrawlerMultiClient(RemoteService.build(urls));
	}

}
