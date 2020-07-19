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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.common.CrawlSessionStatus;
import com.qwazr.utils.TimeTracker;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class WebCrawlSessionStatus extends CrawlSessionStatus<WebCrawlSessionStatus> {

    @JsonCreator
    WebCrawlSessionStatus(@JsonProperty("node_address") String nodeAddress, @JsonProperty("aborting") Boolean aborting,
                          @JsonProperty("aborting_reason") String abortingReason, @JsonProperty("timer") TimeTracker.Status timer,
                          @JsonProperty("crawled") Integer crawled, @JsonProperty("ignored") Integer ignored,
                          @JsonProperty("redirect") Integer redirect, @JsonProperty("error") Integer error,
                          @JsonProperty("last_error") String lastError, @JsonProperty("current_crawl") String currentCrawl,
                          @JsonProperty("start_time") final Long startTime, @JsonProperty("end_time") final Long endTime,
                          @JsonProperty("current_depth") Integer currentDepth,
                          @JsonProperty("thread_cancelled") Boolean threadCancelled,
                          @JsonProperty("thread_done") Boolean threadDone) {
        super(WebCrawlSessionStatus.class, nodeAddress, aborting, abortingReason, timer, crawled, ignored, redirect, error, lastError, currentCrawl,
                startTime, endTime, currentDepth, threadCancelled, threadDone);
    }

    private WebCrawlSessionStatus(Builder builder) {
        super(WebCrawlSessionStatus.class, builder);
    }

    public static Builder of(String nodeAddress, TimeTracker timeTracker) {
        return new Builder(nodeAddress, timeTracker);
    }

    public static class Builder extends AbstractBuilder<WebCrawlSessionStatus, Builder> {

        private Builder(String nodeAddress, TimeTracker timeTracker) {
            super(nodeAddress, timeTracker);
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        public WebCrawlSessionStatus build() {
            return new WebCrawlSessionStatus(this);
        }
    }

}
