/**   
 * License Agreement for QWAZR
 *
 * Copyright (C) 2014-2015 OpenSearchServer Inc.
 * 
 * http://www.qwazr.com
 * 
 * This file is part of QWAZR.
 *
 * QWAZR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * QWAZR is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QWAZR. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.qwazr.crawler.web.service;

import java.net.URISyntaxException;
import java.util.TreeMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.qwazr.crawler.web.manager.WebCrawlerManager;
import com.qwazr.utils.server.ServerException;

public class WebCrawlerServiceImpl implements WebCrawlerServiceInterface {

	@Override
	public TreeMap<String, WebCrawlStatus> getSessions(Boolean local) {

		// Read the sessions in the local node
		TreeMap<String, WebCrawlStatus> localSessions = WebCrawlerManager.INSTANCE
				.getSessions();
		if (local != null && local)
			return localSessions;

		// Read the sessions present in the remote nodes
		try {
			TreeMap<String, WebCrawlStatus> globalSessions = new TreeMap<String, WebCrawlStatus>();
			if (localSessions != null)
				globalSessions.putAll(localSessions);
			globalSessions.putAll(WebCrawlerManager.getClient(true)
					.getSessions(false));
			return globalSessions;
		} catch (URISyntaxException e) {
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public WebCrawlStatus getSession(String session_name, Boolean local) {
		try {
			WebCrawlStatus status = WebCrawlerManager.INSTANCE
					.getSession(session_name);
			if (status != null)
				return status;
			if (local != null && local)
				throw new ServerException(Status.NOT_FOUND, "Session not found");
			return WebCrawlerManager.getClient(true).getSession(session_name,
					false);
		} catch (URISyntaxException | ServerException e) {
			throw ServerException.getJsonException(e);
		}

	}

	@Override
	public Response abortSession(String session_name, Boolean local) {
		try {
			if (local != null && local) {
				WebCrawlerManager.INSTANCE.abortSession(session_name);
				return Response.accepted().build();
			}
			return WebCrawlerManager.getClient(false).abortSession(
					session_name, false);
		} catch (ServerException | URISyntaxException e) {
			throw ServerException.getTextException(e);
		}
	}

	@Override
	public WebCrawlStatus runSession(String session_name,
			WebCrawlDefinition crawlDefinition) {
		try {
			return WebCrawlerManager.INSTANCE.runSession(session_name,
					crawlDefinition);
		} catch (ServerException e) {
			throw ServerException.getTextException(e);
		}
	}
}
