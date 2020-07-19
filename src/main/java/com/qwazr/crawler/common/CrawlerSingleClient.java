/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.server.client.JsonClient;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.SortedMap;

abstract public class CrawlerSingleClient<
        DEFINITION extends CrawlDefinition<DEFINITION>,
        STATUS extends CrawlSessionStatus<STATUS>>
        extends JsonClient
        implements CrawlerServiceInterface<DEFINITION, STATUS> {

    private final Class<STATUS> crawlStatusClass;
    private final Class<DEFINITION> crawlDefinitionClass;
    private final GenericType<SortedMap<String, STATUS>> mapStatusType;
    private final WebTarget sessionsTarget;

    protected CrawlerSingleClient(final RemoteService remote,
                                  final String pathPrefix,
                                  final Class<STATUS> crawlStatusClass,
                                  final Class<DEFINITION> crawlDefinitionClass,
                                  final GenericType<SortedMap<String, STATUS>> mapStatusType) {
        super(remote);
        this.sessionsTarget = client.target(remote.serviceAddress).path(pathPrefix).path("sessions");
        this.crawlStatusClass = crawlStatusClass;
        this.crawlDefinitionClass = crawlDefinitionClass;
        this.mapStatusType = mapStatusType;

    }

    @Override
    public SortedMap<String, STATUS> getSessions() {
        return sessionsTarget.request(MediaType.APPLICATION_JSON).get(mapStatusType);
    }

    @Override
    public STATUS getSessionStatus(final String sessionName) {
        return sessionsTarget
                .path(sessionName)
                .request(MediaType.APPLICATION_JSON)
                .get(crawlStatusClass);
    }

    @Override
    public DEFINITION getSessionDefinition(final String sessionName) {
        return sessionsTarget
                .path(sessionName)
                .path("definition")
                .request(MediaType.APPLICATION_JSON)
                .get(crawlDefinitionClass);
    }

    @Override
    public void stopSession(final String sessionName, final String reason) {
        final WebTarget target = sessionsTarget.path(sessionName);
        if (reason != null)
            target.queryParam("reason", reason);
        target.request(MediaType.TEXT_PLAIN).delete();
    }

    @Override
    public void removeSession(final String sessionName) {
        sessionsTarget
                .path(sessionName)
                .path("definition").
                request(MediaType.TEXT_PLAIN)
                .delete();
    }

    @Override
    public STATUS runSession(final String sessionName, final DEFINITION crawlDefinition) {
        return sessionsTarget.path(sessionName)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(crawlDefinition), crawlStatusClass);
    }

}
