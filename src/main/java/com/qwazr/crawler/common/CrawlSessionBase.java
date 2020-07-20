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

import com.qwazr.utils.TimeTracker;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public abstract class CrawlSessionBase<
        SESSION extends CrawlSessionBase<SESSION, THREAD, MANAGER, DEFINITION, STATUS, ITEM>,
        THREAD extends CrawlThread<THREAD, DEFINITION, STATUS, MANAGER, SESSION, ITEM>,
        MANAGER extends CrawlManager<MANAGER, THREAD, SESSION, DEFINITION, STATUS, ITEM>,
        DEFINITION extends CrawlDefinition<DEFINITION>,
        STATUS extends CrawlSessionStatus<STATUS>,
        ITEM extends CrawlItem<?>
        > implements CrawlSession<DEFINITION, STATUS, ITEM> {

    private final MANAGER crawlManager;
    private final DEFINITION crawlDefinition;
    private final String name;
    private final AtomicBoolean abort;
    private final TimeTracker timeTracker;
    private final CrawlSessionStatus.AbstractBuilder<STATUS, ?> crawlStatusBuilder;
    private final CrawlCollector<ITEM> crawlCollector;
    private volatile STATUS crawlStatus;
    protected final DB sessionDB;

    protected CrawlSessionBase(final String sessionName,
                               final MANAGER crawlManager,
                               final TimeTracker timeTracker,
                               final DEFINITION crawlDefinition,
                               final CrawlSessionStatus.AbstractBuilder<STATUS, ?> crawlStatusBuilder,
                               final CrawlCollectorFactory<ITEM, DEFINITION> collectorFactory) {
        this.sessionDB = DBMaker
                .fileDB(crawlManager.sessionsDirectory.resolve(sessionName).toFile())
                .transactionEnable()
                .make();
        this.crawlManager = crawlManager;
        this.timeTracker = timeTracker;
        this.crawlStatusBuilder = crawlStatusBuilder;
        this.crawlDefinition = crawlDefinition;
        this.name = sessionName;
        abort = new AtomicBoolean(false);
        crawlCollector = collectorFactory == null ? crawlItem -> {
        } : collectorFactory.createCrawlCollector(crawlDefinition);
        buildStatus();
    }

    private void buildStatus() {
        crawlStatus = crawlStatusBuilder.build();
        crawlManager.setSessionStatus(name, crawlStatus);
    }

    @Override
    public STATUS getCrawlStatus() {
        return crawlStatus;
    }

    @Override
    public void collect(ITEM crawlItem) {
        crawlCollector.collect(crawlItem);
    }

    @Override
    public void sleep(int millis) {
        try {
            if (millis == 0)
                return;
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            abort(e.getMessage());
        }
    }

    @Override
    public void abort(String reason) {
        if (abort.getAndSet(true))
            return;
        crawlStatusBuilder.abort(reason);
        buildStatus();
    }

    @Override
    public boolean isAborting() {
        return abort.get();
    }

    public void incRejectedCount() {
        crawlStatusBuilder.incRejected();
        buildStatus();
    }

    public int incCrawledCount() {
        crawlStatusBuilder.incCrawled();
        buildStatus();
        return crawlStatus.crawled;
    }

    public void incRedirectCount() {
        crawlStatusBuilder.incRedirect();
        buildStatus();
    }

    public void incErrorCount(String errorMessage) {
        crawlStatusBuilder.lastError(errorMessage).incError();
        buildStatus();
    }

    public void error(Exception e) {
        crawlStatusBuilder.lastError(ExceptionUtils.getRootCauseMessage(e));
        buildStatus();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setCurrentCrawl(String currentCrawl, Integer currentDepth) {
        crawlStatusBuilder.crawl(currentCrawl, currentDepth);
        buildStatus();
    }

    public DEFINITION getCrawlDefinition() {
        return crawlDefinition;
    }

    public TimeTracker getTimeTracker() {
        return timeTracker;
    }

    void done() {
        crawlStatusBuilder.done();
        buildStatus();
    }

    @Override
    public void close() {
        if (!sessionDB.isClosed())
            sessionDB.close();
    }

}
