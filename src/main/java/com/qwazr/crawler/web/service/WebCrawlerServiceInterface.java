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
import com.qwazr.utils.server.ServiceInterface;
import com.qwazr.utils.server.ServiceName;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.TreeMap;

@RolesAllowed(WebCrawlerManager.SERVICE_NAME_WEBCRAWLER)
@Path("/crawler/web")
@ServiceName(WebCrawlerManager.SERVICE_NAME_WEBCRAWLER)
public interface WebCrawlerServiceInterface extends ServiceInterface {

	@GET
	@Path("/sessions")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	TreeMap<String, WebCrawlStatus> getSessions(@QueryParam("local") Boolean local, @QueryParam("group") String group,
					@QueryParam("timeout") Integer msTimeout);

	@GET
	@Path("/sessions/{session_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	WebCrawlStatus getSession(@PathParam("session_name") String session_name, @QueryParam("local") Boolean local,
					@QueryParam("group") String group, @QueryParam("timeout") Integer msTimeout);

	@DELETE
	@Path("/sessions/{session_name}")
	Response abortSession(@PathParam("session_name") String session_name, @QueryParam("reason") String aborting_reason,
					@QueryParam("local") Boolean local, @QueryParam("group") String group,
					@QueryParam("timeout") Integer msTimeout);

	@POST
	@Path("/sessions/{session_name}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	WebCrawlStatus runSession(@PathParam("session_name") String session_name, WebCrawlDefinition crawlDefinition);

	WebCrawlStatus runSession(String session_name, String jsonCrawlDefinition) throws IOException;

}
