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

import com.qwazr.crawler.web.manager.WebCrawlerManager;
import com.qwazr.utils.server.ServerException;

import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.TreeMap;

public class WebCrawlerClusterServiceImpl extends WebCrawlerSingleServiceImpl {

	@Override
	public TreeMap<String, WebCrawlStatus> getSessions(Boolean local, String group, Integer msTimeout) {

		// Read the sessions in the local node
		if (local != null && local)
			return super.getSessions(local, group, msTimeout);

		// Read the sessions present in the remote nodes
		try {
			TreeMap<String, WebCrawlStatus> globalSessions = new TreeMap<String, WebCrawlStatus>();
			globalSessions.putAll(WebCrawlerManager.getInstance().getMultiClient(group, msTimeout)
					.getSessions(false, group, msTimeout));
			return globalSessions;
		} catch (URISyntaxException e) {
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public WebCrawlStatus getSession(String session_name, Boolean local, String group, Integer msTimeout) {
		try {
			if (local != null && local)
				return super.getSession(session_name, local, group, msTimeout);
			return WebCrawlerManager.getInstance().getMultiClient(group, msTimeout)
					.getSession(session_name, false, group, msTimeout);
		} catch (URISyntaxException e) {
			throw ServerException.getJsonException(e);
		}

	}

	@Override
	public Response abortSession(String session_name, String reason, Boolean local, String group, Integer msTimeout) {
		try {
			if (local != null && local)
				return super.abortSession(session_name, reason, local, group, msTimeout);
			return WebCrawlerManager.getInstance().getMultiClient(group, msTimeout)
					.abortSession(session_name, reason, false, group, msTimeout);
		} catch (URISyntaxException e) {
			throw ServerException.getTextException(e);
		}
	}

}
