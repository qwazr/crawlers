/*
 * Copyright 2015-2021 Emmanuel Keller / QWAZR
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
 */
package com.qwazr.crawler.common;

import com.qwazr.server.ServiceInterface;
import java.util.LinkedHashMap;
import java.util.function.IntConsumer;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

public interface CrawlerServiceInterface<
        DEFINITION extends CrawlDefinition<DEFINITION>,
        STATUS extends CrawlSessionStatus<STATUS>> extends ServiceInterface {

    String X_PAGES_HEADER = "X-pages";

    @GET
    @Path("/sessions")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    default LinkedHashMap<String, STATUS> getSessions(final @Context HttpServletResponse servletResponse,
                                                      final @QueryParam("query") String wildcardQuery,
                                                      final @QueryParam("start") Integer start,
                                                      final @QueryParam("rows") Integer rows) {
        return getSessions(wildcardQuery, start, rows,
                total -> servletResponse.addHeader(X_PAGES_HEADER, Integer.toString(total)));
    }

    LinkedHashMap<String, STATUS> getSessions(final String wildcardQuery,
                                              final Integer start,
                                              final Integer rows,
                                              final IntConsumer totalConsumer);

    @GET
    @Path("/sessions/{session_name}")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    STATUS getSessionStatus(@PathParam("session_name") String sessionName);

    @GET
    @Path("/sessions/{session_name}/definition")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    DEFINITION getSessionDefinition(@PathParam("session_name") String sessionName);

    @DELETE
    @Path("/sessions/{session_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8})
    void stopSession(@PathParam("session_name") String sessionName,
                     @QueryParam("reason") String abortingReason);

    @DELETE
    @Path("/sessions/{session_name}/definition")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    void removeSession(@PathParam("session_name") String sessionName);

    @PUT
    @Path("/sessions/{session_name}")
    @Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    STATUS upsertSession(@PathParam("session_name") String sessionName, DEFINITION crawlDefinition);

    @POST
    @Path("/sessions/{session_name}")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    STATUS runSession(@PathParam("session_name") String sessionName);
}
