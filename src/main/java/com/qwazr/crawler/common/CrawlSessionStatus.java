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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.Equalizer;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public abstract class CrawlSessionStatus<STATUS extends CrawlSessionStatus<STATUS>> extends Equalizer.Immutable<STATUS> {

    /**
     * The identifier of the current node
     */
    @JsonProperty("node_address")
    final public String nodeAddress;

    /**
     * Is the session currently running ?
     */
    final public Boolean running;

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
     * The number of rejected crawl items
     */
    final public int rejected;

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

    protected CrawlSessionStatus(final Class<STATUS> statusClass,
                                 final @JsonProperty("node_address") String nodeAddress,
                                 final @JsonProperty("running") Boolean running,
                                 final @JsonProperty("aborting") Boolean aborting,
                                 final @JsonProperty("aborting_reason") String abortingReason,
                                 final @JsonProperty("crawled") Integer crawled,
                                 final @JsonProperty("rejected") Integer rejected,
                                 final @JsonProperty("redirect") Integer redirect,
                                 final @JsonProperty("error") Integer error,
                                 final @JsonProperty("last_error") String lastError,
                                 final @JsonProperty("current_crawl") String currentCrawl,
                                 final @JsonProperty("start_time") Long startTime,
                                 final @JsonProperty("end_time") Long endTime,
                                 final @JsonProperty("current_depth") Integer currentDepth) {
        super(statusClass);
        this.nodeAddress = nodeAddress;
        this.running = running;
        this.aborting = aborting;
        this.abortingReason = abortingReason;
        this.crawled = crawled == null ? 0 : crawled;
        this.rejected = rejected == null ? 0 : rejected;
        this.redirect = redirect == null ? 0 : redirect;
        this.error = error == null ? 0 : error;
        this.lastError = lastError;
        this.currentCrawl = currentCrawl;
        this.currentDepth = currentDepth;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    protected CrawlSessionStatus(final Class<STATUS> statusClass,
                                 final AbstractBuilder<STATUS, ?> builder) {
        super(statusClass);
        this.nodeAddress = builder.nodeAddress;
        this.running = builder.running;
        this.aborting = builder.aborting;
        this.abortingReason = builder.abortingReason;
        this.crawled = builder.crawled;
        this.rejected = builder.rejected;
        this.redirect = builder.redirect;
        this.error = builder.error;
        this.lastError = builder.lastError;
        this.currentCrawl = builder.currentCrawl;
        this.currentDepth = builder.currentDepth;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
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
     * @return The number of rejected crawl items
     */
    public int getRejected() {
        return rejected;
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

    @Override
    protected int computeHashCode() {
        return Objects.hash(nodeAddress, startTime, endTime, currentCrawl, currentDepth, running, aborting);
    }

    @Override
    public String toString() {
        return "Addr: " + nodeAddress
                + " - running: " + running
                + " - aborting: " + aborting
                + " - abortingReason: " + abortingReason
                + " - crawled: " + crawled
                + " - rejected: " + rejected
                + " - error: " + error
                + " - lastError: " + lastError
                + " - currentCrawl: " + currentCrawl
                + " - currentDepth: " + currentDepth
                + " - startTime: " + startTime
                + " - endTime: " + endTime;
    }

    @Override
    protected boolean isEqual(final STATUS s) {
        return Objects.equals(nodeAddress, s.nodeAddress)
                && Objects.equals(running, s.running)
                && Objects.equals(aborting, s.aborting)
                && Objects.equals(abortingReason, s.abortingReason)
                && crawled == s.crawled && rejected == s.rejected && error == s.error
                && Objects.equals(lastError, s.lastError)
                && Objects.equals(currentCrawl, s.currentCrawl)
                && Objects.equals(currentDepth, s.currentDepth)
                && Objects.equals(startTime, s.startTime)
                && Objects.equals(endTime, s.endTime);
    }

    public abstract static class AbstractBuilder<
            STATUS extends CrawlSessionStatus<STATUS>,
            BUILDER extends AbstractBuilder<STATUS, BUILDER>> {

        private final String nodeAddress;

        private Boolean running;
        private Boolean aborting;
        private String abortingReason;
        private int crawled;
        private int rejected;
        private int redirect;
        private int error;
        private String lastError;
        private String currentCrawl;
        private Integer currentDepth;
        private Long startTime;
        private Long endTime;

        protected AbstractBuilder(final String nodeAddress) {
            this.nodeAddress = nodeAddress;
            this.running = false;
        }

        protected abstract BUILDER me();

        public BUILDER start() {
            if (startTime != null)
                return me();
            this.running = true;
            this.startTime = System.currentTimeMillis();
            return me();
        }

        public BUILDER abort(String abortingReason) {
            if (endTime != null)
                return me();
            this.aborting = true;
            this.abortingReason = abortingReason;
            return me();
        }

        public BUILDER incCrawled() {
            assert endTime == null;
            crawled++;
            return me();
        }

        public BUILDER incRejected() {
            assert endTime == null;
            rejected++;
            return me();
        }

        public BUILDER incRedirect() {
            assert endTime == null;
            redirect++;
            return me();
        }

        public BUILDER incError() {
            assert endTime == null;
            error++;
            return me();
        }

        public BUILDER lastError(String errorMessage) {
            assert endTime == null;
            this.lastError = errorMessage;
            return me();
        }

        public BUILDER crawl(String currentCrawl, Integer currentDepth) {
            assert endTime == null;
            this.currentCrawl = currentCrawl;
            this.currentDepth = currentDepth;
            return me();
        }

        public BUILDER done() {
            assert endTime == null;
            this.endTime = System.currentTimeMillis();
            this.running = false;
            return me();
        }

        public abstract STATUS build();
    }

}
