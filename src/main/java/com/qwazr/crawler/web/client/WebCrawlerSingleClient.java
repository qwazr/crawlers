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
package com.qwazr.crawler.web.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.crawler.web.service.WebCrawlStatus;
import com.qwazr.crawler.web.service.WebCrawlerServiceInterface;
import com.qwazr.utils.http.HttpResponseEntityException;
import com.qwazr.utils.http.HttpUtils;
import com.qwazr.utils.json.client.JsonClientAbstract;
import com.qwazr.utils.server.RemoteService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.TreeMap;

public class WebCrawlerSingleClient extends JsonClientAbstract implements WebCrawlerServiceInterface {

	public WebCrawlerSingleClient(final RemoteService remote) {
		super(remote);
	}

	public final static TypeReference<TreeMap<String, WebCrawlStatus>> TreeMapStringCrawlTypeRef =
			new TypeReference<TreeMap<String, WebCrawlStatus>>() {
			};

	@Override
	public TreeMap<String, WebCrawlStatus> getSessions(Boolean local, String group, Integer msTimeout) {
		UBuilder uriBuilder = new UBuilder("/crawler/web/sessions").setParameters(local, group, msTimeout);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, TreeMapStringCrawlTypeRef, 200);
	}

	@Override
	public WebCrawlStatus getSession(String session_name, Boolean local, String group, Integer msTimeout) {
		UBuilder uriBuilder =
				new UBuilder("/crawler/web/sessions/", session_name).setParameters(local, group, msTimeout);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, WebCrawlStatus.class, 200);
	}

	@Override
	public Response abortSession(String session_name, String reason, Boolean local, String group, Integer msTimeout) {
		try {
			UBuilder uriBuilder =
					new UBuilder("/crawler/web/sessions/", session_name).setParameters(local, group, msTimeout)
							.setParameterObject("reason", reason);
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200, 202);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public WebCrawlStatus runSession(String session_name, WebCrawlDefinition crawlDefinition) {
		UBuilder uriBuilder = new UBuilder("/crawler/web/sessions/", session_name);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, crawlDefinition, null, WebCrawlStatus.class, 200, 202);
	}

	@Override
	public WebCrawlStatus runSession(String session_name, String jsonCrawlDefinition) throws IOException {
		return runSession(session_name, WebCrawlDefinition.newInstance(jsonCrawlDefinition));
	}

}
