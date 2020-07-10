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

import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.WildcardMatcher;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public abstract class CrawlThread<
        THREAD extends CrawlThread<THREAD, DEFINITION, STATUS, MANAGER, SESSION, ITEM>,
        DEFINITION extends CrawlDefinition<DEFINITION>,
        STATUS extends CrawlStatus<STATUS>,
        MANAGER extends CrawlManager<MANAGER, THREAD, SESSION, DEFINITION, STATUS, ITEM>,
        SESSION extends CrawlSessionBase<SESSION, THREAD, MANAGER, DEFINITION, STATUS, ITEM>,
        ITEM extends CrawlItem
        > implements Runnable {

    private final CrawlDefinition<DEFINITION> crawlDefinition;
    private final TimeTracker timeTracker;
    protected final MANAGER manager;
    protected final SESSION session;
    protected final Logger logger;
    private final List<WildcardMatcher> exclusionMatcherList;
    private final List<WildcardMatcher> inclusionMatcherList;

    protected CrawlThread(final MANAGER manager,
                          final SESSION session,
                          final Logger logger) {
        this.manager = manager;
        this.session = session;
        this.crawlDefinition = session.getCrawlDefinition();
        this.timeTracker = session.getTimeTracker();
        this.logger = logger;
        this.exclusionMatcherList = WildcardMatcher.getList(crawlDefinition.exclusionPatterns);
        this.inclusionMatcherList = WildcardMatcher.getList(crawlDefinition.inclusionPatterns);
    }

    public String getSessionName() {
        return session.getName();
    }

    protected abstract void runner() throws Exception;

    /**
     * Check the matching list. Returns null if the matching list is empty.
     *
     * @param text     the text to check
     * @param matchers a list of wildcard patterns
     * @return true if a pattern matched the text
     **/
    @Nullable
    protected static Boolean matches(String text, List<WildcardMatcher> matchers) {
        if (matchers == null || matchers.isEmpty())
            return null;
        return WildcardMatcher.anyMatch(text, matchers);
    }

    protected boolean checkPassInclusionExclusion(String itemText, Consumer<Boolean> inclusionConsumer,
                                                  Consumer<Boolean> exclusionConsumer) {

        // We check the inclusion/exclusion.
        final Boolean inInclusion = matches(itemText, inclusionMatcherList);
        final Boolean inExclusion = matches(itemText, exclusionMatcherList);
        if (inclusionConsumer != null)
            inclusionConsumer.accept(inInclusion);
        if (exclusionConsumer != null)
            exclusionConsumer.accept(inExclusion);

        return (inInclusion == null || inInclusion) && (inExclusion == null || !inExclusion);
    }

    protected void checkPassInclusionExclusion(CrawlItemBase.BaseBuilder<?> current, String itemText) {
        if (!checkPassInclusionExclusion(itemText, current::inInclusion, current::inExclusion)) {
            current.ignored(true);
            session.incIgnoredCount();
        } else {
            current.crawled(true);
            session.incCrawledCount();
        }
    }

    @Override
    final public void run() {
        try {
            runner();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e, e::getMessage);
            session.error(e);
        } finally {
            session.done();
            manager.removeSession(session.getName());
        }
    }

    protected final STATUS getStatus() {
        return session.getCrawlStatus();
    }

    protected void start() {
        session.setFuture(manager.executorService.submit(this));
    }

    protected void abort(final String abortingReason) {
        session.abort(abortingReason);
    }

}
