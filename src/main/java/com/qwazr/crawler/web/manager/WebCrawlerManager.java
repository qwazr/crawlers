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
import com.qwazr.crawler.web.service.*;
import com.qwazr.utils.LockUtils;
import com.qwazr.utils.server.ServerException;
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

	public final static String SERVICE_NAME_WEBCRAWLER = "webcrawler";

	private static final Logger logger = LoggerFactory.getLogger(WebCrawlerManager.class);

	static WebCrawlerManager INSTANCE = null;

	private final ExecutorService executorService;

	public synchronized static Class<? extends WebCrawlerServiceInterface> load(ExecutorService executor)
			throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		try {
			INSTANCE = new WebCrawlerManager(executor);
			return ClusterManager.INSTANCE.isCluster() ?
					WebCrawlerClusterServiceImpl.class :
					WebCrawlerSingleServiceImpl.class;
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

	private WebCrawlerManager(ExecutorService executor) throws IOException, URISyntaxException {
		this.executorService = executor;
		crawlSessionMap = new HashMap<String, WebCrawlThread>();
	}

	public TreeMap<String, WebCrawlStatus> getSessions() {
		TreeMap<String, WebCrawlStatus> map = new TreeMap<String, WebCrawlStatus>();
		rwlSessionMap.r.lock();
		try {
			for (Map.Entry<String, WebCrawlThread> entry : crawlSessionMap.entrySet())
				map.put(entry.getKey(), entry.getValue().getStatus());
			return map;
		} finally {
			rwlSessionMap.r.unlock();
		}
	}

	public WebCrawlStatus getSession(String session_name) {
		rwlSessionMap.r.lock();
		try {
			WebCrawlThread crawlThread = crawlSessionMap.get(session_name);
			if (crawlThread == null)
				return null;
			return crawlThread.getStatus();
		} finally {
			rwlSessionMap.r.unlock();
		}
	}

	public void abortSession(String session_name, String abortingReason) throws ServerException {
		rwlSessionMap.r.lock();
		try {
			WebCrawlThread crawlThread = crawlSessionMap.get(session_name);
			if (crawlThread == null)
				throw new ServerException(Status.NOT_FOUND, "Session not found: " + session_name);
			if (logger.isInfoEnabled())
				logger.info("Aborting session: " + session_name + " - " + abortingReason);
			crawlThread.abort(abortingReason);
		} finally {
			rwlSessionMap.r.unlock();
		}
	}

	public WebCrawlStatus runSession(String session_name, WebCrawlDefinition crawlJson) throws ServerException {
		rwlSessionMap.w.lock();
		try {
			if (crawlSessionMap.containsKey(session_name))
				throw new ServerException(Status.CONFLICT, "The session already exists: " + session_name);
			if (logger.isInfoEnabled())
				logger.info("Create session: " + session_name);

			WebCrawlThread crawlThread = new WebCrawlThread(session_name, crawlJson);
			crawlSessionMap.put(session_name, crawlThread);
			executorService.execute(crawlThread);
			return crawlThread.getStatus();
		} finally {
			rwlSessionMap.w.unlock();
		}
	}

	void removeSession(WebCrawlThread crawlThread) {
		rwlSessionMap.w.lock();
		try {
			String sessionName = crawlThread.getSessionName();
			if (logger.isInfoEnabled())
				logger.info("Remove session: " + sessionName);
			crawlSessionMap.remove(sessionName, crawlThread);
		} finally {
			rwlSessionMap.w.unlock();
		}
	}

	public WebCrawlerMultiClient getMultiClient(String group, Integer msTimeout) throws URISyntaxException {
		String[] urls = ClusterManager.INSTANCE.getClusterClient()
				.getActiveNodesByService(SERVICE_NAME_WEBCRAWLER, group);
		return new WebCrawlerMultiClient(executorService, urls, msTimeout);
	}

}
