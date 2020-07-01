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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.TimeTracker;

import java.util.Objects;
import java.util.concurrent.Future;

@JsonInclude(Include.NON_NULL)
public abstract class CrawlStatus<T extends CrawlDefinition<T>, S extends CrawlStatus<T, S>> extends Equalizer.Immutable<S> {

    /**
     * The identifier of the current node
     */
    @JsonProperty("node_address")
    final public String nodeAddress;

    /**
     * Is the session currently aborting ?
     */
    final public Boolean aborting;

    /**
     * the aborting reason if any
     */
    @JsonProperty("aborting_reason")
    final public String abortingReason;

    /**
     * The number of crawled items
     */
    final public int crawled;

    /**
     * The number of ignored crawl items
     */
    final public int ignored;

    /**
     * The number of redirect (or alias)
     */
    final public int redirect;

    /**
     * The number of erroneous crawls
     */
    final public int error;

    @JsonProperty("last_error")
    final public String lastError;

    /**
     * the current crawl item
     */
    @JsonProperty("current_crawl")
    final public String currentCrawl;

    /**
     * the depth of the current crawled item
     */
    @JsonProperty("current_depth")
    final public Integer currentDepth;

    @JsonProperty("start_time")
    final public Long startTime;

    @JsonProperty("end_time")
    final public Long endTime;

    @JsonProperty("thread_cancelled")
    final public Boolean threadCancelled;

    @JsonProperty("thread_done")
    final public Boolean threadDone;

    final public TimeTracker.Status timer;

    /**
     * the attributes of the session
     */
    @JsonProperty("crawl_definition")
    final public T crawlDefinition;

    @JsonCreator
    protected CrawlStatus(final Class<S> statusClass,
                          @JsonProperty("node_address") String nodeAddress,
                          @JsonProperty("aborting") Boolean aborting,
                          @JsonProperty("aborting_reason") String abortingReason, @JsonProperty("timer") TimeTracker.Status timer,
                          @JsonProperty("crawled") Integer crawled, @JsonProperty("ignored") Integer ignored,
                          @JsonProperty("redirect") Integer redirect, @JsonProperty("error") Integer error,
                          @JsonProperty("last_error") String lastError, @JsonProperty("current_crawl") String currentCrawl,
                          @JsonProperty("start_time") final Long startTime, @JsonProperty("end_time") final Long endTime,
                          @JsonProperty("current_depth") Integer currentDepth, @JsonProperty("crawl_definition") T crawlDefinition,
                          @JsonProperty("thread_cancelled") Boolean threadCancelled,
                          @JsonProperty("thread_done") Boolean threadDone) {
        super(statusClass);
        this.nodeAddress = nodeAddress;
        this.timer = timer;
        this.aborting = aborting;
        this.abortingReason = abortingReason;
        this.crawled = crawled == null ? 0 : crawled;
        this.ignored = ignored == null ? 0 : ignored;
        this.redirect = redirect == null ? 0 : redirect;
        this.error = error == null ? 0 : error;
        this.lastError = lastError;
        this.currentCrawl = currentCrawl;
        this.currentDepth = currentDepth;
        this.startTime = startTime;
        this.endTime = endTime;
        this.crawlDefinition = crawlDefinition;
        this.threadCancelled = threadCancelled;
        this.threadDone = threadDone;
    }

    protected CrawlStatus(final Class<S> statusClass,
                          final AbstractBuilder<T, ?, ?> builder, boolean withCrawlDefinition) {
        super(statusClass);
        this.nodeAddress = builder.nodeAddress;
        this.timer = builder.timeTracker == null ? null : builder.timeTracker.getStatus();
        this.aborting = builder.aborting;
        this.abortingReason = builder.abortingReason;
        this.crawled = builder.crawled;
        this.ignored = builder.ignored;
        this.redirect = builder.redirect;
        this.error = builder.error;
        this.lastError = builder.lastError;
        this.currentCrawl = builder.currentCrawl;
        this.currentDepth = builder.currentDepth;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.crawlDefinition = withCrawlDefinition ? builder.crawlDefinition : null;
        if (builder.future == null) {
            this.threadDone = null;
            this.threadCancelled = null;
        } else {
            this.threadDone = builder.future.isDone();
            this.threadCancelled = builder.future.isCancelled();
        }
    }

    /**
     * @return The identifier of the current node
     */
    @JsonIgnore
    public String getNodeAddress() {
        return nodeAddress;
    }

    /**
     * @return Is the session currently aborting ?
     */
    public Boolean getAborting() {
        return aborting;
    }

    /**
     * @return the aborting reason if any
     */
    @JsonIgnore
    public String getAbortingReason() {
        return abortingReason;
    }

    /**
     * @return The number of crawled items
     */
    public int getCrawled() {
        return crawled;
    }

    /**
     * @return The number of ignored crawl items
     */
    public int getIgnored() {
        return ignored;
    }

    /**
     * @return The number of redirects
     */
    public int getRedirect() {
        return redirect;
    }

    /**
     * @return The number of erroneous crawls
     */
    public int getError() {
        return error;
    }

    @JsonIgnore
    public String getLastError() {
        return lastError;
    }

    /**
     * @return the current crawl item
     */
    @JsonIgnore
    public String getCurrentCrawl() {
        return currentCrawl;
    }

    /**
     * @return the depth of the current crawled item
     */
    @JsonIgnore
    public Integer getCurrentDepth() {
        return currentDepth;
    }

    @JsonIgnore
    public Long getStartTime() {
        return startTime;
    }

    @JsonIgnore
    public Long getEndTime() {
        return endTime;
    }

    public TimeTracker.Status getTimer() {
        return timer;
    }

    @JsonIgnore
    public T getCrawlDefinition() {
        return crawlDefinition;
    }

    @JsonIgnore
    public Boolean getThreadCancelled() {
        return threadCancelled;
    }

    @JsonIgnore
    public Boolean getThreadDone() {
        return threadDone;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(nodeAddress, startTime, endTime, currentCrawl, currentDepth, crawlDefinition);
    }

    @Override
    public String toString() {
        return nodeAddress
                + " " + startTime
                + " " + aborting
                + " " + abortingReason
                + " " + crawled
                + " " + ignored
                + " " + error
                + " " + lastError
                + " " + currentCrawl
                + " " + currentDepth
                + " " + endTime
                + " " + timer
                + " " + crawlDefinition
                + " " + threadCancelled
                + " " + threadDone;
    }

    @Override
    protected boolean isEqual(final S s) {
        return Objects.equals(nodeAddress, s.nodeAddress) && Objects.equals(startTime, s.startTime) &&
                Objects.equals(aborting, s.aborting) && Objects.equals(abortingReason, s.abortingReason) &&
                crawled == s.crawled && ignored == s.ignored && error == s.error &&
                Objects.equals(lastError, s.lastError) && Objects.equals(currentCrawl, s.currentCrawl) &&
                Objects.equals(currentDepth, s.currentDepth) && Objects.equals(endTime, s.endTime) &&
                Objects.equals(timer, s.timer) && Objects.equals(crawlDefinition, s.crawlDefinition) &&
                Objects.equals(threadCancelled, s.threadCancelled) && Objects.equals(threadDone, s.threadDone);
    }

    public abstract static class AbstractBuilder<D extends CrawlDefinition<D>, S extends CrawlStatus<D, S>, B extends AbstractBuilder<D, S, ?>> {

        private final Class<B> builderClass;
        private final String nodeAddress;
        private final long startTime;
        private final TimeTracker timeTracker;
        private final D crawlDefinition;

        private Boolean aborting;
        private String abortingReason;
        private int crawled;
        private int ignored;
        private int redirect;
        private int error;
        private String lastError;
        private String currentCrawl;
        private Integer currentDepth;
        private Long endTime;
        private volatile Future<?> future;

        protected AbstractBuilder(final Class<B> builderClass, final String nodeAddress, final TimeTracker timeTracker,
                                  final D crawlDefinition) {
            this.builderClass = builderClass;
            this.nodeAddress = nodeAddress;
            this.startTime = System.currentTimeMillis();
            this.timeTracker = timeTracker;
            this.crawlDefinition = crawlDefinition;
        }

        public B abort(String abortingReason) {
            this.aborting = true;
            this.abortingReason = abortingReason;
            return builderClass.cast(this);
        }

        public B incCrawled() {
            crawled++;
            return builderClass.cast(this);
        }

        public B incIgnored() {
            ignored++;
            return builderClass.cast(this);
        }

        public B incRedirect() {
            redirect++;
            return builderClass.cast(this);
        }

        public B incError() {
            error++;
            return builderClass.cast(this);
        }

        public B lastError(String errorMessage) {
            this.lastError = errorMessage;
            return builderClass.cast(this);
        }

        public B crawl(String currentCrawl, Integer currentDepth) {
            this.currentCrawl = currentCrawl;
            this.currentDepth = currentDepth;
            return builderClass.cast(this);
        }

        public B done() {
            this.endTime = System.currentTimeMillis();
            return builderClass.cast(this);
        }

        B future(Future<?> future) {
            this.future = future;
            return builderClass.cast(this);
        }

        public abstract S build(boolean withCrawlDefinition);
    }

}
