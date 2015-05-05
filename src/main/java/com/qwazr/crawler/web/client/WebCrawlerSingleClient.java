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
