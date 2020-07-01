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
package com.qwazr.crawler.common;

import com.qwazr.scripts.ScriptManager;
import com.qwazr.scripts.ScriptServiceInterface;
import com.qwazr.server.ServerException;
import com.qwazr.utils.CollectionsUtils;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public abstract class CrawlManager<T extends CrawlThread<D, S, ?>, D extends CrawlDefinition<D>, S extends CrawlStatus<D, S>>
        extends AttributesBase {

    private final Map<String, S> statusHistory;
    private final ConcurrentHashMap<String, T> crawlSessionMap;
    protected final ExecutorService executorService;
    private final Logger logger;
    protected final String myAddress;
    protected final ScriptServiceInterface scriptService;

    protected CrawlManager(final String myAddress, final ScriptManager scriptManager,
                           final ExecutorService executorService, final Logger logger) {
        this.crawlSessionMap = new ConcurrentHashMap<>();
        this.statusHistory = Collections.synchronizedMap(new CollectionsUtils.EldestFixedSizeMap<>(250));
        this.scriptService = scriptManager == null ? null : scriptManager.getService();
        this.myAddress = myAddress;
        this.executorService = executorService;
        this.logger = logger;
    }

    public void forEachSession(BiConsumer<String, S> consumer) {
        crawlSessionMap.forEach((key, crawl) -> consumer.accept(key, crawl.getStatus(false)));
    }

    public S getSession(final String sessionName) {
        final T crawlThread = crawlSessionMap.get(sessionName);
        return crawlThread == null ? statusHistory.get(sessionName) : crawlThread.getStatus(true);
    }

    public void abortSession(final String sessionName, final String abortingReason) throws ServerException {
        final T crawlThread = crawlSessionMap.get(sessionName);
        if (crawlThread == null)
            throw new ServerException(Response.Status.NOT_FOUND, "Session not found: " + sessionName);
        logger.info(() -> "Aborting session: " + sessionName + " - " + abortingReason);
        crawlThread.abort(abortingReason);
    }

    protected abstract T newCrawlThread(final String sessionName,
                                        final D crawlDefinition);

    public S runSession(final String sessionName, final D crawlDefinition) throws ServerException {

        final AtomicBoolean newThread = new AtomicBoolean(false);

        final T crawlThread = crawlSessionMap.computeIfAbsent(sessionName, key -> {
            logger.info(() -> "Create session: " + sessionName);
            newThread.set(true);
            return newCrawlThread(sessionName, crawlDefinition);
        });
        statusHistory.remove(sessionName);
        if (!newThread.get())
            throw new ServerException(Response.Status.CONFLICT, "The session already exists: " + sessionName);

        crawlThread.start();
        return crawlThread.getStatus(true);
    }

    void removeSession(final CrawlThread<D, S, ?> crawlThread) {
        final String sessionName = crawlThread.getSessionName();
        logger.info(() -> "Remove session: " + sessionName);
        final S lastCrawlStatus = crawlThread.getStatus(false);
        statusHistory.put(sessionName, lastCrawlStatus);
        crawlSessionMap.remove(sessionName, crawlThread);
    }

}
