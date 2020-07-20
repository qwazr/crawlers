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

import com.qwazr.utils.WildcardMatcher;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.validation.constraints.NotNull;

public abstract class CrawlThread<
        THREAD extends CrawlThread<THREAD, DEFINITION, STATUS, MANAGER, SESSION, ITEM>,
        DEFINITION extends CrawlDefinition<DEFINITION>,
        STATUS extends CrawlSessionStatus<STATUS>,
        MANAGER extends CrawlManager<MANAGER, THREAD, SESSION, DEFINITION, STATUS, ITEM>,
        SESSION extends CrawlSessionBase<SESSION, THREAD, MANAGER, DEFINITION, STATUS, ITEM>,
        ITEM extends CrawlItem<?>
        > implements Runnable {

    protected final MANAGER manager;
    protected final SESSION session;
    protected final Logger logger;
    @NotNull
    private final Map<WildcardMatcher, WildcardFilter.Status> filters;
    @NotNull
    private final WildcardFilter.Status filterPolicy;

    protected CrawlThread(final MANAGER manager,
                          final SESSION session,
                          final Logger logger) {
        this.manager = manager;
        this.session = session;
        final DEFINITION crawlDefinition = session.getCrawlDefinition();
        this.logger = logger;
        this.filters = WildcardFilter.compileFilters(crawlDefinition.filters);
        this.filterPolicy = WildcardFilter.definePolicy(crawlDefinition.filterPolicy, filters);
    }

    public String getSessionName() {
        return session.getName();
    }

    protected abstract void runner() throws Exception;

    protected Rejected checkWildcardFilters(final String itemText) {
        return WildcardFilter.match(itemText, filters, filterPolicy);
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
            session.close();
        }
    }

    protected void abort(final String abortingReason) {
        session.abort(abortingReason);
    }

}
