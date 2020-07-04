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
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public abstract class CrawlSessionBase<
        SESSION extends CrawlSessionBase<SESSION, THREAD, MANAGER, DEFINITION, STATUS, BUILDER>,
        THREAD extends CrawlThread<THREAD, DEFINITION, STATUS, BUILDER, MANAGER, SESSION>,
        MANAGER extends CrawlManager<MANAGER, THREAD, SESSION, DEFINITION, STATUS, BUILDER>,
        DEFINITION extends CrawlDefinition<DEFINITION>,
        STATUS extends CrawlStatus<STATUS>,
        BUILDER extends CrawlStatus.AbstractBuilder<STATUS, BUILDER>>
        extends AttributesBase implements CrawlSession<DEFINITION, STATUS> {

    private final MANAGER crawlManager;
    private final DEFINITION crawlDefinition;
    private final String name;
    private final AtomicBoolean abort;
    private final TimeTracker timeTracker;
    private final BUILDER crawlStatusBuilder;
    private volatile STATUS crawlStatus;
    protected final DB sessionDB;

    protected CrawlSessionBase(final String sessionName,
                               final MANAGER crawlManager,
                               final TimeTracker timeTracker,
                               final DEFINITION crawlDefinition,
                               final Map<String, Object> attributes,
                               final BUILDER crawlStatusBuilder) {
        super(attributes);
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
    public <V> V getVariable(final String name, final Class<? extends V> variableClass) {
        return crawlDefinition != null && crawlDefinition.variables != null ?
                variableClass.cast(crawlDefinition.variables.get(name)) :
                null;
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

    public void incIgnoredCount() {
        crawlStatusBuilder.incIgnored();
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

    void setFuture(Future<?> future) {
        crawlStatusBuilder.future(future);
        buildStatus();
    }

    @Override
    public void close() {
        crawlStatusBuilder.abort(null);
        buildStatus();
        sessionDB.commit();
    }
}
