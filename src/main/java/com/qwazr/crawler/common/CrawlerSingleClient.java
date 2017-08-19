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
	private final Class<? extends CrawlStatus<T>> crawlStatusClass;

	protected CrawlerSingleClient(final RemoteService remote, final String pathPrefix,
			Class<? extends CrawlStatus<T>> crawlStatusClass) {
		super(remote);
		this.pathPrefix = pathPrefix;
		this.crawlStatusClass = crawlStatusClass;
	}

	@Override
	public TreeMap<String, CrawlStatus> getSessions() {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, pathPrefix + "sessions");
		HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, TreeMapStringCrawlTypeRef, valid200Json);
	}

	@Override
	public CrawlStatus<T> getSession(final String sessionName) {
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
	public CrawlStatus<T> runSession(final String sessionName, final T crawlDefinition) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, pathPrefix + "sessions/", sessionName);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, crawlDefinition, null, crawlStatusClass, valid200202Json);
	}

}
