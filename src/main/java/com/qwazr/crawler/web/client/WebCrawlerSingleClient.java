/**
 * Copyright 2014-2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.crawler.web.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.TreeMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.crawler.web.service.WebCrawlStatus;
import com.qwazr.crawler.web.service.WebCrawlerServiceInterface;
import com.qwazr.utils.http.HttpResponseEntityException;
import com.qwazr.utils.http.HttpUtils;
import com.qwazr.utils.json.client.JsonClientAbstract;

public class WebCrawlerSingleClient extends JsonClientAbstract implements
		WebCrawlerServiceInterface {

	WebCrawlerSingleClient(String url, int msTimeOut) throws URISyntaxException {
		super(url, msTimeOut);
	}

	public final static TypeReference<TreeMap<String, WebCrawlStatus>> TreeMapStringCrawlTypeRef = new TypeReference<TreeMap<String, WebCrawlStatus>>() {
	};

	@Override
	public TreeMap<String, WebCrawlStatus> getSessions(Boolean local) {
		try {
			URIBuilder uriBuilder = getBaseUrl("/crawler/web/sessions");
			if (local != null)
				uriBuilder.setParameter("local", local.toString());
			Request request = Request.Get(uriBuilder.build());
			return (TreeMap<String, WebCrawlStatus>) execute(request, null,
					msTimeOut, TreeMapStringCrawlTypeRef, 200);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (URISyntaxException | IOException e) {
			throw new WebApplicationException(e.getMessage(), e,
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public WebCrawlStatus getSession(String session_name, Boolean local) {
		try {
			URIBuilder uriBuilder = getBaseUrl("/crawler/web/sessions/",
					session_name);
			if (local != null)
				uriBuilder.setParameter("local", local.toString());
			Request request = Request.Get(uriBuilder.build());
			return execute(request, null, msTimeOut, WebCrawlStatus.class, 200);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (URISyntaxException | IOException e) {
			throw new WebApplicationException(e.getMessage(), e,
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response abortSession(String session_name, Boolean local) {
		try {
			URIBuilder uriBuilder = getBaseUrl("/crawler/web/sessions/",
					session_name);
			if (local != null)
				uriBuilder.setParameter("local", local.toString());
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, msTimeOut);
			HttpUtils.checkStatusCodes(response, 200, 202);
			return Response.status(response.getStatusLine().getStatusCode())
					.build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (URISyntaxException | IOException e) {
			throw new WebApplicationException(e.getMessage(), e,
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public WebCrawlStatus runSession(String session_name,
			WebCrawlDefinition crawlDefinition) {
		try {
			URIBuilder uriBuilder = getBaseUrl("/crawler/web/sessions/",
					session_name);
			Request request = Request.Post(uriBuilder.build());
			return execute(request, crawlDefinition, msTimeOut,
					WebCrawlStatus.class, 200, 202);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (URISyntaxException | IOException e) {
			throw new WebApplicationException(e.getMessage(), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

}
