/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.crawler.web.manager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.crawler.web.WebCrawlerServer;
import com.qwazr.crawler.web.client.WebCrawlerMultiClient;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.crawler.web.service.WebCrawlStatus;
import com.qwazr.utils.LockUtils;
import com.qwazr.utils.server.AbstractServer;
import com.qwazr.utils.server.ServerException;

public class WebCrawlerManager {

	private static final Logger logger = LoggerFactory
			.getLogger(WebCrawlerManager.class);

	public static volatile WebCrawlerManager INSTANCE = null;

	public static void load(AbstractServer server, File directory)
			throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		try {
			INSTANCE = new WebCrawlerManager();
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	private final LockUtils.ReadWriteLock rwlSessionMap = new LockUtils.ReadWriteLock();
	private final HashMap<String, WebCrawlThread> crawlSessionMap;
	private final ThreadGroup threadGroup;

	private WebCrawlerManager() throws IOException, URISyntaxException {
		threadGroup = new ThreadGroup("CrawlThreads");
		crawlSessionMap = new HashMap<String, WebCrawlThread>();
	}

	public TreeMap<String, WebCrawlStatus> getSessions() {
		TreeMap<String, WebCrawlStatus> map = new TreeMap<String, WebCrawlStatus>();
		rwlSessionMap.r.lock();
		try {
			for (Map.Entry<String, WebCrawlThread> entry : crawlSessionMap
					.entrySet())
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

	public void abortSession(String session_name) throws ServerException {
		rwlSessionMap.r.lock();
		try {
			WebCrawlThread crawlThread = crawlSessionMap.get(session_name);
			if (crawlThread == null)
				throw new ServerException(Status.NOT_FOUND,
						"Session not found: " + session_name);
			logger.info("Aborting session: " + session_name);
			crawlThread.abort();
		} finally {
			rwlSessionMap.r.unlock();
		}
	}

	public WebCrawlStatus runSession(String session_name,
			WebCrawlDefinition crawlJson) throws ServerException {
		rwlSessionMap.w.lock();
		try {
			if (crawlSessionMap.containsKey(session_name))
				throw new ServerException(Status.CONFLICT,
						"The session already exists: " + session_name);
			logger.info("Create session: " + session_name);
			WebCrawlThread crawlThread = new WebCrawlThread(threadGroup,
					session_name, crawlJson);
			crawlSessionMap.put(session_name, crawlThread);
			crawlThread.start();
			return crawlThread.getStatus();
		} finally {
			rwlSessionMap.w.unlock();
		}
	}

	void removeSession(WebCrawlThread crawlThread) {
		rwlSessionMap.w.lock();
		try {
			String sessionName = crawlThread.getSessionName();
			logger.info("Remove session: " + sessionName);
			crawlSessionMap.remove(sessionName, crawlThread);
		} finally {
			rwlSessionMap.w.unlock();
		}
	}

	public static WebCrawlerMultiClient getClient() throws URISyntaxException {
		return new WebCrawlerMultiClient(ClusterManager.INSTANCE
				.getClusterClient().getActiveNodes(
						WebCrawlerServer.SERVICE_NAME_WEBCRAWLER), 60000);
	}
}
