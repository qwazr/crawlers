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

import com.qwazr.crawler.web.WebCrawlStatus;
import com.qwazr.server.AbstractServiceImpl;
import com.qwazr.server.ServerException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.TreeMap;
import java.util.logging.Logger;

public abstract class CrawlerServiceImpl<M extends CrawlManager<?, D, S>, D extends CrawlDefinition, S extends CrawlStatus<D>>
		extends AbstractServiceImpl implements CrawlerServiceInterface<D, S> {

	protected final Logger logger;

	protected final M crawlManager;

	protected CrawlerServiceImpl(Logger logger, M crawlManager) {
		this.logger = logger;
		this.crawlManager = crawlManager;
	}

	@Override
	public TreeMap<String, S> getSessions() {
		final TreeMap<String, S> map = new TreeMap<>();
		crawlManager.forEachSession((name, status) -> map.put(name, (S) status));
		return map;
	}

	public S getSession(final String sessionName) {
		try {
			final S status = crawlManager.getSession(sessionName);
			if (status != null)
				return status;
			throw new ServerException(Status.NOT_FOUND, "Session not found");
		} catch (ServerException e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	public Response abortSession(final String sessionName, final String reason) {
		try {
			crawlManager.abortSession(sessionName, reason);
			return Response.accepted().build();
		} catch (ServerException e) {
			throw ServerException.getTextException(logger, e);
		}
	}

	public S runSession(final String session_name, final D crawlDefinition) {
		try {
			return crawlManager.runSession(session_name, crawlDefinition);
		} catch (ServerException e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

}
