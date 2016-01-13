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
package com.qwazr.crawler.web.service;

import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.crawler.web.manager.WebCrawlerManager;
import com.qwazr.utils.server.ServerException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.TreeMap;

public class WebCrawlerSingleServiceImpl implements WebCrawlerServiceInterface {

	@Override
	public TreeMap<String, WebCrawlStatus> getSessions(Boolean local, String group, Integer msTimeout) {
		// Read the sessions in the local node
		if (!ClusterManager.INSTANCE.isGroup(group))
			return new TreeMap<String, WebCrawlStatus>();
		return WebCrawlerManager.getInstance().getSessions();
	}

	@Override
	public WebCrawlStatus getSession(String session_name, Boolean local, String group, Integer msTimeout) {
		try {
			WebCrawlStatus status = ClusterManager.INSTANCE.isGroup(group) ?
					WebCrawlerManager.getInstance().getSession(session_name) :
					null;
			if (status != null)
				return status;
			throw new ServerException(Status.NOT_FOUND, "Session not found");
		} catch (ServerException e) {
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response abortSession(String session_name, String reason, Boolean local, String group, Integer msTimeout) {
		try {
			if (!ClusterManager.INSTANCE.isGroup(group))
				throw new ServerException(Status.NOT_FOUND, "Session not found");
			WebCrawlerManager.getInstance().abortSession(session_name, reason);
			return Response.accepted().build();
		} catch (ServerException e) {
			throw ServerException.getTextException(e);
		}
	}

	@Override
	public WebCrawlStatus runSession(String session_name, WebCrawlDefinition crawlDefinition) {
		try {
			return WebCrawlerManager.getInstance().runSession(session_name, crawlDefinition);
		} catch (ServerException e) {
			throw ServerException.getJsonException(e);
		}
	}

	public WebCrawlStatus runSession(String session_name, String jsonCrawlDefinition) throws IOException {
		return runSession(session_name, WebCrawlDefinition.newInstance(jsonCrawlDefinition));
	}

}
