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
package com.qwazr.crawler.web.service;

import java.util.TreeMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/crawler/web")
public interface WebCrawlerServiceInterface {

	@GET
	@Path("/sessions")
	@Produces(MediaType.APPLICATION_JSON)
	public TreeMap<String, WebCrawlStatus> getSessions(
			@QueryParam("local") Boolean local);

	@GET
	@Path("/sessions/{session_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public WebCrawlStatus getSession(
			@PathParam("session_name") String session_name,
			@QueryParam("local") Boolean local);

	@DELETE
	@Path("/sessions/{session_name}")
	public Response abortSession(
			@PathParam("session_name") String session_name,
			@QueryParam("local") Boolean local);

	@POST
	@PUT
	@Path("/sessions/{session_name}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WebCrawlStatus runSession(
			@PathParam("session_name") String session_name,
			WebCrawlDefinition crawlDefinition);

}
