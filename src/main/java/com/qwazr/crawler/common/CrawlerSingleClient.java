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

abstract public class CrawlerSingleClient<D extends CrawlDefinition<D>, S extends CrawlStatus<D, S>> extends JsonClient
        implements CrawlerServiceInterface<D, S> {

    private final Class<S> crawlStatusClass;
    private final GenericType<SortedMap<String, S>> mapStatusType;
    private final WebTarget sessionsTarget;

    protected CrawlerSingleClient(final RemoteService remote, final String pathPrefix, Class<S> crawlStatusClass,
                                  GenericType<SortedMap<String, S>> mapStatusType) {
        super(remote);
        this.sessionsTarget = client.target(remote.serviceAddress).path(pathPrefix).path("sessions");
        this.crawlStatusClass = crawlStatusClass;
        this.mapStatusType = mapStatusType;

    }

    @Override
    public SortedMap<String, S> getSessions() {
        return sessionsTarget.request(MediaType.APPLICATION_JSON).get(mapStatusType);
    }

    @Override
    public S getSession(final String sessionName) {
        return sessionsTarget.path(sessionName).request(MediaType.APPLICATION_JSON).get(crawlStatusClass);
    }

    @Override
    public boolean abortSession(final String sessionName, final String reason) {
        WebTarget target = sessionsTarget.path(sessionName);
        if (reason != null)
            target.queryParam("reason", reason);
        return target.request(MediaType.TEXT_PLAIN).delete(boolean.class);
    }

    @Override
    public S runSession(final String sessionName, final D crawlDefinition) {
        return sessionsTarget.path(sessionName)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(crawlDefinition), crawlStatusClass);
    }

}
