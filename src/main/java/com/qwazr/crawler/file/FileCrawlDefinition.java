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
 **/
package com.qwazr.crawler.file;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.common.CrawlDefinition;
import com.qwazr.crawler.common.WildcardFilter;
import com.qwazr.utils.ObjectMappers;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class FileCrawlDefinition extends CrawlDefinition<FileCrawlDefinition> {

    /**
     * The entry point PATH of the crawl.
     */
    @JsonProperty("entry_path")
    final public String entryPath;

    @JsonCreator
    protected FileCrawlDefinition(final @JsonProperty("entry_path") String entryPath,
                                  final @JsonProperty("max_depth") Integer maxDepth,
                                  final @JsonProperty("filters") LinkedHashMap<String, WildcardFilter.Status> filters,
                                  final @JsonProperty("filter_policy") WildcardFilter.Status filterPolicy,
                                  final @JsonProperty("crawl_wait_ms") Integer crawlWaitMs,
                                  final @JsonProperty("crawl_collector_factory") String crawlCollectorFactoryClass,
                                  final @JsonProperty("variables") LinkedHashMap<String, Object> variables) {
        super(FileCrawlDefinition.class, crawlCollectorFactoryClass, variables,
                filters, filterPolicy, maxDepth, crawlWaitMs);
        this.entryPath = entryPath;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(entryPath, super.computeHashCode());
    }

    @Override
    protected boolean isEqual(final FileCrawlDefinition f) {
        return super.isEqual(f) && Objects.equals(entryPath, f.entryPath);
    }

    public String getEntryPath() {
        return this.entryPath;
    }

    public static Builder of() {
        return new Builder();
    }

    public static Builder of(FileCrawlDefinition crawlDefinition) {
        return new Builder(crawlDefinition);
    }

    public static class Builder extends AbstractBuilder<FileCrawlDefinition, Builder> {

        private String entryPath;

        protected Builder() {
        }

        protected Builder(FileCrawlDefinition crawlDefinition) {
            super(crawlDefinition);
            entryPath = crawlDefinition.entryPath;
            maxDepth = crawlDefinition.maxDepth;
            crawlWaitMs = crawlDefinition.crawlWaitMs;
        }

        public Builder entryPath(final String entryPath) {
            this.entryPath = entryPath;
            return this;
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        public FileCrawlDefinition build() {
            return new FileCrawlDefinition(entryPath, maxDepth, filters, filterPolicy,
                    crawlWaitMs, crawlCollectorFactoryClass, variables);
        }
    }

    @JsonIgnore
    public static FileCrawlDefinition newInstance(final String json) throws IOException {
        return ObjectMappers.JSON.readValue(json, FileCrawlDefinition.class);
    }

}
