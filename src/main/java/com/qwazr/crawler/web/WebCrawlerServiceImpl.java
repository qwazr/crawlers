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

import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.server.AbstractServiceImpl;
import com.qwazr.server.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.TreeMap;

class WebCrawlerServiceImpl extends AbstractServiceImpl implements WebCrawlerServiceInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebCrawlerServiceImpl.class);

	private volatile WebCrawlerManager webrawlerManager;

	WebCrawlerServiceImpl(WebCrawlerManager webrawlerManager) {
		this.webrawlerManager = webrawlerManager;
	}

	public WebCrawlerServiceImpl() {
	}

	@PostConstruct
	public void init() {
		webrawlerManager = getContextAttribute(WebCrawlerManager.class);
	}

	@Override
	public TreeMap<String, CrawlStatus> getSessions(final String group) {
		// Read the sessions in the local node
		if (!webrawlerManager.clusterManager.isGroup(group))
			return new TreeMap<>();
		return webrawlerManager.getSessions();
	}

	@Override
	public CrawlStatus getSession(final String session_name, final String group) {
		try {
			final CrawlStatus status = webrawlerManager.clusterManager.isGroup(group) ? webrawlerManager.getSession(
					session_name) : null;
			if (status != null)
				return status;
			throw new ServerException(Status.NOT_FOUND, "Session not found");
		} catch (ServerException e) {
			throw ServerException.getJsonException(LOGGER, e);
		}
	}

	@Override
	public Response abortSession(final String session_name, final String reason, final String group) {
		try {
			if (!webrawlerManager.clusterManager.isGroup(group))
				throw new ServerException(Status.NOT_FOUND, "Session not found");
			webrawlerManager.abortSession(session_name, reason);
			return Response.accepted().build();
		} catch (ServerException e) {
			throw ServerException.getTextException(LOGGER, e);
		}
	}

	@Override
	public CrawlStatus runSession(final String session_name, final WebCrawlDefinition crawlDefinition) {
		try {
			return webrawlerManager.runSession(session_name, crawlDefinition);
		} catch (ServerException e) {
			throw ServerException.getJsonException(LOGGER, e);
		}
	}

	public CrawlStatus runSession(final String session_name, final String jsonCrawlDefinition) throws IOException {
		return runSession(session_name, WebCrawlDefinition.newInstance(jsonCrawlDefinition));
	}

}
