/*
 * Copyright 2017-2021 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.file;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.common.CrawlSessionStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class FileCrawlSessionStatus extends CrawlSessionStatus<FileCrawlSessionStatus> {

    @JsonCreator
    FileCrawlSessionStatus(final @JsonProperty("node_address") String nodeAddress,
                           final @JsonProperty("running") Boolean running,
                           final @JsonProperty("aborting") Boolean aborting,
                           final @JsonProperty("aborting_reason") String abortingReason,
                           final @JsonProperty("crawled") Integer crawled,
                           final @JsonProperty("ignored") Integer ignored,
                           final @JsonProperty("redirect") Integer redirect,
                           final @JsonProperty("error") Integer error,
                           final @JsonProperty("last_error") String lastError,
                           final @JsonProperty("current_crawl") String currentCrawl,
                           final @JsonProperty("start_time") Long startTime,
                           final @JsonProperty("end_time") Long endTime,
                           final @JsonProperty("current_depth") Integer currentDepth) {
        super(FileCrawlSessionStatus.class, nodeAddress, running, aborting, abortingReason,
                crawled, ignored, redirect,
                error, lastError, currentCrawl, startTime, endTime, currentDepth);
    }

    private FileCrawlSessionStatus(Builder builder) {
        super(FileCrawlSessionStatus.class, builder);
    }

    public static Builder of(String nodeAddress) {
        return new Builder(nodeAddress);
    }

    public static class Builder extends AbstractBuilder<FileCrawlSessionStatus, Builder> {

        private Builder(String nodeAddress) {
            super(nodeAddress);
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        public FileCrawlSessionStatus build() {
            return new FileCrawlSessionStatus(this);
        }
    }

}
