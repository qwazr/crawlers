/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web;

import com.qwazr.crawler.common.CrawlSessionBase;
import com.qwazr.utils.TimeTracker;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

public class WebCrawlSession extends CrawlSessionBase
        <WebCrawlSession, WebCrawlThread, WebCrawlerManager, WebCrawlDefinition, WebCrawlStatus, WebCrawlStatus.Builder> {

    private final HTreeMap.KeySet<String> crawledUrls;
    private final HTreeMap<String, Integer> toCrawlUrls;

    private final Object urlDatabaseLock;

    WebCrawlSession(final String sessionName,
                    final WebCrawlerManager webCrawlerManager,
                    final TimeTracker timeTracker,
                    final WebCrawlDefinition crawlDefinition,
                    final Map<String, Object> attributes,
                    final WebCrawlStatus.Builder crawlStatusBuilder) {
        super(sessionName, webCrawlerManager, timeTracker, crawlDefinition, attributes, crawlStatusBuilder);
        crawledUrls = sessionDB.hashSet("crawled")
                .serializer(Serializer.STRING)
                .createOrOpen();
        toCrawlUrls = sessionDB.hashMap("tocrawl")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.INTEGER)
                .createOrOpen();
        urlDatabaseLock = new Object();
    }

    boolean isCrawled(final String uriString) {
        synchronized (urlDatabaseLock) {
            return crawledUrls.contains(uriString);
        }
    }

    Pair<String, Integer> nextUrlToCrawl() {
        synchronized (urlDatabaseLock) {
            final Iterator<String> keyIterator = toCrawlUrls.keySet().iterator();
            if (!keyIterator.hasNext())
                return null;
            final String url = keyIterator.next();
            final Integer depth = toCrawlUrls.get(url);
            return Pair.of(url, depth);
        }
    }

    void addUrlsToCrawl(final Map<String, Integer> links) {
        if (links == null || links.isEmpty())
            return;
        synchronized (urlDatabaseLock) {
            final Map<String, Integer> toAdd = new LinkedHashMap<>();
            links.forEach((uri, depth) -> {
                if (!crawledUrls.contains(uri))
                    toAdd.put(uri, depth);
            });
            if (!toAdd.isEmpty()) {
                toCrawlUrls.putAll(toAdd);
                sessionDB.commit();
            }
        }
    }

    void addUrlsToCrawl(final Set<URI> links, Integer depth) {
        if (links == null || links.isEmpty())
            return;
        synchronized (urlDatabaseLock) {
            final Map<String, Integer> toAdd = new LinkedHashMap<>();
            links.forEach(uri -> {
                final String uriString = uri.toASCIIString();
                if (!crawledUrls.contains(uriString))
                    toAdd.put(uriString, depth);
            });
            if (!toAdd.isEmpty()) {
                toCrawlUrls.putAll(toAdd);
                sessionDB.commit();
            }
        }
    }

    void setCrawled(final String uriString) {
        synchronized (urlDatabaseLock) {
            toCrawlUrls.remove(uriString);
            crawledUrls.add(uriString);
            sessionDB.commit();
        }
    }
}
