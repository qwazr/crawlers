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

import com.qwazr.crawler.common.CrawlCollector;
import com.qwazr.crawler.common.CrawlSessionBase;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;

public class WebCrawlSession extends CrawlSessionBase
        <WebCrawlSession, WebCrawlThread, WebCrawlerManager, WebCrawlDefinition, WebCrawlSessionStatus, WebCrawlItem> {

    private final HTreeMap.KeySet<String> crawledUrls;
    private final HTreeMap.KeySet<String> toCrawlUrls;
    private final NavigableSet<Object[]> nextToCrawl;

    private final Object urlDatabaseLock;

    WebCrawlSession(final String sessionName,
                    final WebCrawlerManager webCrawlerManager,
                    final WebCrawlDefinition crawlDefinition,
                    final WebCrawlSessionStatus.Builder crawlStatusBuilder,
                    final CrawlCollector<WebCrawlItem> webCrawlCollector) {
        super(sessionName, webCrawlerManager, crawlDefinition, crawlStatusBuilder, webCrawlCollector);
        crawledUrls = sessionDB.hashSet("crawled")
                .serializer(Serializer.STRING)
                .createOrOpen();
        toCrawlUrls = sessionDB.hashSet("tocrawl")
                .serializer(Serializer.STRING)
                .createOrOpen();
        nextToCrawl = sessionDB.treeSet("nextToCrawl")
                .serializer(new SerializerArrayTuple(Serializer.INTEGER, Serializer.STRING))
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
            final Iterator<Object[]> iterator = nextToCrawl.iterator();
            if (!iterator.hasNext())
                return null;
            final Object[] item = iterator.next();
            return Pair.of((String) item[1], (Integer) item[0]);
        }
    }

    private void addUriStringToCrawl(final String uriString, final Integer depth, final AtomicBoolean needCommit) {
        if (crawledUrls.contains(uriString) || toCrawlUrls.contains(uriString))
            return;
        toCrawlUrls.add(uriString);
        nextToCrawl.add(new Object[]{depth, uriString});
        needCommit.set(true);
    }

    void addUrltoCrawl(final URI uri, final Integer depth) {
        if (uri == null)
            return;
        synchronized (urlDatabaseLock) {
            final AtomicBoolean needCommit = new AtomicBoolean(false);
            addUriStringToCrawl(uri.toASCIIString(), depth, needCommit);
            if (needCommit.get())
                sessionDB.commit();
        }
    }


    void addUrlsToCrawl(final Map<String, Integer> links) {
        if (links == null || links.isEmpty())
            return;
        synchronized (urlDatabaseLock) {
            final AtomicBoolean needCommit = new AtomicBoolean(false);
            links.forEach((uri, depth) -> addUriStringToCrawl(uri, depth, needCommit));
            if (needCommit.get())
                sessionDB.commit();
        }
    }

    void addUrlsToCrawl(final Set<URI> links, final Integer depth) {
        if (links == null || links.isEmpty())
            return;
        synchronized (urlDatabaseLock) {
            final AtomicBoolean needCommit = new AtomicBoolean(false);
            links.forEach(uri -> addUriStringToCrawl(uri.toASCIIString(), depth, needCommit));
            if (needCommit.get())
                sessionDB.commit();
        }
    }

    void setCrawled(final String uriString, final Integer depth) {
        setCurrentCrawl(uriString, depth);
        synchronized (urlDatabaseLock) {
            toCrawlUrls.remove(uriString);
            crawledUrls.add(uriString);
            nextToCrawl.remove(new Object[]{depth, uriString});
            sessionDB.commit();
        }
    }
}
