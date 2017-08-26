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
package com.qwazr.crawler.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.server.RemoteService;
import com.qwazr.server.client.JsonClientAbstract;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.UBuilder;
import com.qwazr.utils.http.HttpRequest;

import javax.ws.rs.core.Response;
import java.util.TreeMap;
import java.util.logging.Logger;

abstract public class CrawlerSingleClient<D extends CrawlDefinition, S extends CrawlStatus<D>>
		extends JsonClientAbstract implements CrawlerServiceInterface<D, S> {

	private final static Logger LOGGER = LoggerUtils.getLogger(JsonClientAbstract.class);

	private final String pathPrefix;
	private final Class<S> crawlStatusClass;
	private final TypeReference<TreeMap<String, S>> mapStatusType;

	protected CrawlerSingleClient(final RemoteService remote, final String pathPrefix, Class<S> crawlStatusClass,
			TypeReference<TreeMap<String, S>> mapStatusType) {
		super(remote, LOGGER);
		this.pathPrefix = pathPrefix;
		this.crawlStatusClass = crawlStatusClass;
		this.mapStatusType = mapStatusType;

	}

	@Override
	public TreeMap<String, S> getSessions() {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, pathPrefix + "sessions");
		HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, mapStatusType, valid200Json);
	}

	@Override
	public S getSession(final String sessionName) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, pathPrefix + "sessions/", sessionName);
		HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, crawlStatusClass, valid200Json);
	}

	@Override
	public Response abortSession(final String sessionName, final String reason) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, pathPrefix + "sessions/", sessionName)
				.setParameter("reason", reason);
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		final Integer statusCode = executeStatusCode(request, null, null, valid200202);
		return Response.status(statusCode).build();
	}

	@Override
	public S runSession(final String sessionName, final D crawlDefinition) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, pathPrefix + "sessions/", sessionName);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, crawlDefinition, null, crawlStatusClass, valid200202Json);
	}

}
