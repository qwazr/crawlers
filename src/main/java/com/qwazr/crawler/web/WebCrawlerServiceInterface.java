/**
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.server.ServiceInterface;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.TreeMap;

@RolesAllowed(WebCrawlerServiceInterface.SERVICE_NAME)
@Path("/crawler/web")
public interface WebCrawlerServiceInterface extends ServiceInterface {

	String SERVICE_NAME = "webcrawler";

	@GET
	@Path("/sessions")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	TreeMap<String, WebCrawlStatus> getSessions(@QueryParam("group") String group);

	@GET
	@Path("/sessions/{session_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	WebCrawlStatus getSession(@PathParam("session_name") String session_name, @QueryParam("group") String group);

	@DELETE
	@Path("/sessions/{session_name}")
	Response abortSession(@PathParam("session_name") String session_name, @QueryParam("reason") String aborting_reason,
			@QueryParam("group") String group);

	@POST
	@Path("/sessions/{session_name}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	WebCrawlStatus runSession(@PathParam("session_name") String session_name, WebCrawlDefinition crawlDefinition);

	WebCrawlStatus runSession(String session_name, String jsonCrawlDefinition) throws IOException;

	TypeReference<TreeMap<String, WebCrawlStatus>> TreeMapStringCrawlTypeRef =
			new TypeReference<TreeMap<String, WebCrawlStatus>>() {
			};

}
