/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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

public interface CrawlSession<
        DEFINITION extends CrawlDefinition<DEFINITION>,
        STATUS extends CrawlStatus<STATUS>> extends AttributesInterface, AutoCloseable {

    /**
     * @return the current crawl status
     */
    STATUS getCrawlStatus();

    /**
     * @return the crawl definition
     */
    DEFINITION getCrawlDefinition();

    /**
     * @param name the name of the variable
     * @param <V>  the type of the variable
     * @return the value of the variable
     */
    <V> V getVariable(String name, Class<? extends V> variableClass);

    /**
     * Causes the currently executing thread to sleep
     *
     * @param millis the number of milliseconds
     */
    void sleep(int millis);

    /**
     * Call this method to abort the current session and set the reason.
     * If it was previously aborted, the reason is not updated
     *
     * @param reason the motivation of the abort
     */
    void abort(String reason);

    /**
     * Check if the session is currently aborting
     *
     * @return true if the session is aborting
     */
    boolean isAborting();

    /**
     * @return the name of the session
     */
    String getName();

    /**
     * @return time information about the current crawl session
     */
    TimeTracker getTimeTracker();

    void close();
}
