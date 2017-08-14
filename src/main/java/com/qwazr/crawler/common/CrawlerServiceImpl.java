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
package com.qwazr.crawler.common;

import com.qwazr.server.AbstractServiceImpl;
import com.qwazr.server.ServerException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.TreeMap;
import java.util.logging.Logger;

public abstract class CrawlerServiceImpl<M extends CrawlManager, T extends CrawlDefinition> extends AbstractServiceImpl
		implements CrawlerServiceInterface<T> {

	protected final Logger logger;

	protected final M crawlManager;

	protected CrawlerServiceImpl(Logger logger, M crawlManager) {
		this.logger = logger;
		this.crawlManager = crawlManager;
	}

	@Override
	public TreeMap<String, CrawlStatus> getSessions(final String group) {
		// Read the sessions in the local node
		if (!crawlManager.getClusterManager().isGroup(group))
			return new TreeMap<>();
		return crawlManager.getSessions();
	}

	@Override
	public CrawlStatus getSession(final String session_name, final String group) {
		try {
			final CrawlStatus status =
					crawlManager.getClusterManager().isGroup(group) ? crawlManager.getSession(session_name) : null;
			if (status != null)
				return status;
			throw new ServerException(Status.NOT_FOUND, "Session not found");
		} catch (ServerException e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	public Response abortSession(final String session_name, final String reason, final String group) {
		try {
			if (!crawlManager.getClusterManager().isGroup(group))
				throw new ServerException(Status.NOT_FOUND, "Session not found");
			crawlManager.abortSession(session_name, reason);
			return Response.accepted().build();
		} catch (ServerException e) {
			throw ServerException.getTextException(logger, e);
		}
	}

	@Override
	public CrawlStatus runSession(final String session_name, final T crawlDefinition) {
		try {
			return crawlManager.runSession(session_name, crawlDefinition);
		} catch (ServerException e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

}
