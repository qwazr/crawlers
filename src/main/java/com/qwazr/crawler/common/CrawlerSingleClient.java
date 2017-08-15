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

import com.qwazr.server.RemoteService;
import com.qwazr.server.client.JsonClientAbstract;
import com.qwazr.utils.UBuilder;
import com.qwazr.utils.http.HttpRequest;

import javax.ws.rs.core.Response;
import java.util.TreeMap;

abstract public class CrawlerSingleClient<T extends CrawlDefinition> extends JsonClientAbstract
		implements CrawlerServiceInterface<T> {

	private final String pathPrefix;

	protected CrawlerSingleClient(final RemoteService remote, final String pathPrefix) {
		super(remote);
		this.pathPrefix = pathPrefix;
	}

	@Override
	public TreeMap<String, CrawlStatus> getSessions(final String group) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, pathPrefix + "sessions").setParameter("group", group);
		HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, TreeMapStringCrawlTypeRef, valid200Json);
	}

	@Override
	public CrawlStatus getSession(final String session_name, final String group) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, pathPrefix + "sessions/", session_name)
				.setParameter("group", group);
		HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, CrawlStatus.class, valid200Json);
	}

	@Override
	public Response abortSession(final String session_name, final String reason, final String group) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, pathPrefix + "sessions/", session_name)
				.setParameter("group", group)
				.setParameterObject("reason", reason);
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		final Integer statusCode = executeStatusCode(request, null, null, valid200202);
		return Response.status(statusCode).build();
	}

	@Override
	public CrawlStatus runSession(final String session_name, final T crawlDefinition) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, pathPrefix + "sessions/", session_name);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, crawlDefinition, null, CrawlStatus.class, valid200202Json);
	}

}