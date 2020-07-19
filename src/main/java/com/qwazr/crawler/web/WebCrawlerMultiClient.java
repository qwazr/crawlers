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
package com.qwazr.crawler.web;

import com.qwazr.crawler.common.CrawlerSingleClient;
import com.qwazr.server.RemoteService;
import com.qwazr.server.client.MultiClient;
import com.qwazr.server.client.MultiWebApplicationException;
import com.qwazr.utils.LoggerUtils;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class WebCrawlerMultiClient extends MultiClient<WebCrawlerSingleClient> implements WebCrawlerServiceInterface {

    private static final Logger logger = LoggerUtils.getLogger(WebCrawlerMultiClient.class);

    public WebCrawlerMultiClient(final ExecutorService executorService, final RemoteService... remotes) {
        super(getClients(remotes), executorService);
    }

    private static WebCrawlerSingleClient[] getClients(final RemoteService... remotes) {
        final WebCrawlerSingleClient[] clients = new WebCrawlerSingleClient[remotes.length];
        int i = 0;
        for (RemoteService remote : remotes)
            clients[i++] = new WebCrawlerSingleClient(remote);
        return clients;
    }

    @Override
    public SortedMap<String, WebCrawlSessionStatus> getSessions() {

        final MultiWebApplicationException.Builder exceptions = MultiWebApplicationException.of(logger);
        final List<SortedMap<String, WebCrawlSessionStatus>> results =
                forEachParallel(CrawlerSingleClient::getSessions, exceptions::add);
        if (results != null && !results.isEmpty()) {
            // We merge the result of all the nodes
            final TreeMap<String, WebCrawlSessionStatus> result = new TreeMap<>();
            results.forEach(result::putAll);
            return result;
        }
        if (!exceptions.isEmpty())
            throw exceptions.build();
        return Collections.emptySortedMap();
    }

    @Override
    public WebCrawlSessionStatus getSessionStatus(final String sessionName) {
        return firstRandomSuccess(c -> c.getSessionStatus(sessionName), logger);
    }

    @Override
    public WebCrawlDefinition getSessionDefinition(final String sessionName) {
        return firstRandomSuccess(c -> c.getSessionDefinition(sessionName), logger);
    }

    @Override
    public void stopSession(final String sessionName, final String reason) {
        firstRandomSuccess(c -> {
            c.stopSession(sessionName, reason);
            return null;
        }, logger);
    }

    @Override
    public void removeSession(final String sessionName) {
        firstRandomSuccess(c -> {
            c.removeSession(sessionName);
            return null;
        }, logger);
    }

    @Override
    public WebCrawlSessionStatus runSession(final String sessionName, final WebCrawlDefinition crawlDefinition) {
        return firstRandomSuccess(c -> c.runSession(sessionName, crawlDefinition), logger);

    }

}
