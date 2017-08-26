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
 **/
package com.qwazr.crawler.web;

import com.qwazr.server.RemoteService;
import com.qwazr.server.ServerException;
import com.qwazr.server.client.MultiClient;
import com.qwazr.utils.LoggerUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebCrawlerMultiClient extends MultiClient<WebCrawlerSingleClient> implements WebCrawlerServiceInterface {

	private static final Logger logger = LoggerUtils.getLogger(WebCrawlerMultiClient.class);

	public WebCrawlerMultiClient(final ExecutorService executorService, final RemoteService... remotes) {
		super(getClients(remotes), executorService);
	}

	private static WebCrawlerSingleClient[] getClients(final RemoteService... remotes) {
		final WebCrawlerSingleClient[] clients = new WebCrawlerSingleClient[remotes.length];
		int i = 0;
		for (RemoteService remote : remotes)
			clients[i++] = new WebCrawlerSingleClient(remote);
		return clients;
	}

	@Override
	public TreeMap<String, WebCrawlStatus> getSessions() {

		// We merge the result of all the nodes
		final TreeMap<String, WebCrawlStatus> globalSessions = new TreeMap<>();
		for (WebCrawlerSingleClient client : this) {
			try {
				final TreeMap<String, WebCrawlStatus> localSessions = client.getSessions();
				if (localSessions == null)
					continue;
				globalSessions.putAll(localSessions);
			} catch (WebApplicationException e) {
				logger.log(Level.WARNING, e, e::getMessage);
			}
		}
		return globalSessions;
	}

	@Override
	public WebCrawlStatus getSession(String sessionName) {

		for (WebCrawlerSingleClient client : this) {
			try {
				return client.getSession(sessionName);
			} catch (WebApplicationException e) {
				if (e.getResponse().getStatus() != 404)
					throw e;
			}
		}
		throw new ServerException(Status.NOT_FOUND, "Session " + sessionName + " not found").getJsonException(false);
	}

	@Override
	public Response abortSession(String sessionName, String reason) {

		boolean aborted = false;
		for (WebCrawlerSingleClient client : this) {
			try {
				int code = client.abortSession(sessionName, reason).getStatus();
				if (code == 200 || code == 202)
					aborted = true;
			} catch (WebApplicationException e) {
				if (e.getResponse().getStatus() != 404)
					throw e;
			}
		}
		return Response.status(aborted ? Status.ACCEPTED : Status.NOT_FOUND).build();
	}

	@Override
	public WebCrawlStatus runSession(final String sessionName, final WebCrawlDefinition crawlDefinition) {
		return firstRandomSuccess(c -> c.runSession(sessionName, crawlDefinition), logger);

	}

	@Override
	public WebCrawlStatus runSession(String session_name, String jsonCrawlDefinition) throws IOException {
		return runSession(session_name, WebCrawlDefinition.newInstance(jsonCrawlDefinition));
	}

}