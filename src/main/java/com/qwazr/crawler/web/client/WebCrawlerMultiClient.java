/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web.client;

import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.crawler.web.service.WebCrawlStatus;
import com.qwazr.crawler.web.service.WebCrawlerServiceInterface;
import com.qwazr.utils.http.HttpResponseEntityException;
import com.qwazr.utils.json.client.JsonMultiClientAbstract;
import com.qwazr.utils.server.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.TreeMap;

public class WebCrawlerMultiClient extends JsonMultiClientAbstract<String, WebCrawlerSingleClient>
				implements WebCrawlerServiceInterface {

	private static final Logger logger = LoggerFactory.getLogger(WebCrawlerMultiClient.class);

	public WebCrawlerMultiClient(String[] urls, Integer msTimeOut) throws URISyntaxException {
		// TODO Pass executor
		super(null, new WebCrawlerSingleClient[urls.length], urls, msTimeOut);
	}

	@Override
	protected WebCrawlerSingleClient newClient(String url, Integer msTimeOut) throws URISyntaxException {
		return new WebCrawlerSingleClient(url, msTimeOut);
	}

	@Override
	public TreeMap<String, WebCrawlStatus> getSessions(Boolean local) {

		// If not global, just request the local node
		if (local != null && local) {
			WebCrawlerSingleClient client = getClientByUrl(ClusterManager.INSTANCE.myAddress);
			if (client == null)
				throw new ServerException(Status.NOT_ACCEPTABLE, "Node not valid: " + ClusterManager.INSTANCE.myAddress)
								.getJsonException();
			return client.getSessions(true);
		}

		// We merge the result of all the nodes
		TreeMap<String, WebCrawlStatus> globalSessions = new TreeMap<String, WebCrawlStatus>();
		for (WebCrawlerSingleClient client : this) {
			try {
				TreeMap<String, WebCrawlStatus> localSessions = client.getSessions(true);
				if (localSessions == null)
					continue;
				globalSessions.putAll(localSessions);
			} catch (WebApplicationException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return globalSessions;
	}

	@Override
	public WebCrawlStatus getSession(String session_name, Boolean local) {

		// If not global, just request the local node
		if (local != null && local) {
			WebCrawlerSingleClient client = getClientByUrl(ClusterManager.INSTANCE.myAddress);
			if (client == null)
				throw new ServerException(Status.NOT_ACCEPTABLE, "Node not valid: " + ClusterManager.INSTANCE.myAddress)
								.getJsonException();
			return client.getSession(session_name, true);
		}

		for (WebCrawlerSingleClient client : this) {
			try {
				return client.getSession(session_name, true);
			} catch (WebApplicationException e) {
				if (e.getResponse().getStatus() != 404)
					throw e;
			}
		}
		throw new ServerException(Status.NOT_FOUND, "Session " + session_name + " not found").getJsonException();
	}

	@Override
	public Response abortSession(String session_name, String reason, Boolean local) {

		// Is it local ?
		if (local != null && local) {
			WebCrawlerSingleClient client = getClientByUrl(ClusterManager.INSTANCE.myAddress);
			if (client == null)
				throw new ServerException(Status.NOT_ACCEPTABLE, "Node not valid: " + ClusterManager.INSTANCE.myAddress)
								.getJsonException();
			return client.abortSession(session_name, reason, true);
		}

		// Global, we abort on every nodes
		boolean aborted = false;
		for (WebCrawlerSingleClient client : this) {
			try {
				int code = client.abortSession(session_name, reason, true).getStatus();
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
	public WebCrawlStatus runSession(String session_name, WebCrawlDefinition crawlDefinition) {
		WebAppExceptionHolder exceptionHolder = new WebAppExceptionHolder(logger);
		for (WebCrawlerSingleClient client : this) {
			try {
				return client.runSession(session_name, crawlDefinition);
			} catch (WebApplicationException e) {
				exceptionHolder.switchAndWarn(e);
			}
		}
		WebApplicationException e = exceptionHolder.getException();
		if (e == null)
			return null;
		HttpResponseEntityException hree = HttpResponseEntityException.findFirstCause(e);
		if (hree != null)
			throw hree.getWebApplicationException();
		throw e;
	}

	@Override
	public WebCrawlStatus runSession(String session_name, String jsonCrawlDefinition) throws IOException {
		return runSession(session_name, WebCrawlDefinition.newInstance(jsonCrawlDefinition));
	}

}