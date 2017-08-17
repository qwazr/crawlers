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
package com.qwazr.crawler.common;

import com.qwazr.scripts.ScriptManager;
import com.qwazr.server.ServerException;
import com.qwazr.utils.CollectionsUtils;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public abstract class CrawlManager<T extends CrawlThread<?>, D extends CrawlDefinition> {

	private final Map<String, CrawlStatus> statusHistory;
	private final ConcurrentHashMap<String, T> crawlSessionMap;
	private final ExecutorService executorService;
	private final Logger logger;
	protected final String myAddress;
	protected final ScriptManager scriptManager;

	protected CrawlManager(final String myAddress, final ScriptManager scriptManager,
			final ExecutorService executorService, final Logger logger) {
		this.crawlSessionMap = new ConcurrentHashMap<>();
		this.statusHistory = Collections.synchronizedMap(new CollectionsUtils.EldestFixedSizeMap<>(250));
		this.scriptManager = scriptManager;
		this.myAddress = myAddress;
		this.executorService = executorService;
		this.logger = logger;
	}

	public String getMyAddress() {
		return myAddress;
	}

	public TreeMap<String, CrawlStatus> getSessions() {
		final TreeMap<String, CrawlStatus> map = new TreeMap<>();
		crawlSessionMap.forEach((key, crawl) -> map.put(key, crawl.getStatus()));
		return map;
	}

	public CrawlStatus getSession(final String sessionName) {
		final T crawlThread = crawlSessionMap.get(sessionName);
		return crawlThread == null ? statusHistory.get(sessionName) : crawlThread.getStatus();
	}

	public void abortSession(final String sessionName, final String abortingReason) throws ServerException {
		final T crawlThread = crawlSessionMap.get(sessionName);
		if (crawlThread == null)
			throw new ServerException(Response.Status.NOT_FOUND, "Session not found: " + sessionName);
		logger.info(() -> "Aborting session: " + sessionName + " - " + abortingReason);
		crawlThread.abort(abortingReason);
	}

	protected abstract T newCrawlThread(final String sessionName, final D crawlDefinition);

	public CrawlStatus runSession(final String sessionName, final D crawlDefinition) throws ServerException {

		final AtomicBoolean newThread = new AtomicBoolean(false);

		final T crawlThread = crawlSessionMap.computeIfAbsent(sessionName, key -> {
			logger.info(() -> "Create session: " + sessionName);
			newThread.set(true);
			return newCrawlThread(sessionName, crawlDefinition);
		});
		statusHistory.remove(sessionName);
		if (!newThread.get())
			throw new ServerException(Response.Status.CONFLICT, "The session already exists: " + sessionName);
		executorService.execute(crawlThread);
		return crawlThread.getStatus();
	}

	void removeSession(final T crawlThread) {
		final String sessionName = crawlThread.getSessionName();
		logger.info(() -> "Remove session: " + sessionName);
		final CrawlStatus lastCrawlStatus = crawlThread.getStatus();
		statusHistory.put(sessionName, lastCrawlStatus);
		crawlSessionMap.remove(sessionName, crawlThread);
	}

}
