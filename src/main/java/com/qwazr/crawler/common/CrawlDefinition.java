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
 */
package com.qwazr.crawler.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.MapUtils;
import com.qwazr.utils.StringUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public abstract class CrawlDefinition<
        DEFINITION extends CrawlDefinition<DEFINITION>
        > extends Equalizer.Immutable<DEFINITION> {

    /**
     * The name of the crawlCollectorFactory class
     */
    @JsonProperty("crawl_collector_factory")
    final public String crawlCollectorFactoryClass;

    /**
     * The users variables shared by all the scripts.
     */
    final public Map<String, Object> variables;


    /**
     * A list of wildcard patterns. An item may be crawled only if it
     * matches any pattern with a FilterStatus sets to 'accept'.
     */
    @JsonProperty("filters")
    final public Map<String, WildcardFilter.Status> filters;

    /**
     * The status applied if no filter matched.
     */
    @JsonProperty("filter_policy")
    final public WildcardFilter.Status filterPolicy;

    /**
     * The maximum number of directory levels to visit.
     */
    @JsonProperty("max_depth")
    final public Integer maxDepth;

    /**
     * Time wait on successful crawl
     */
    @JsonProperty("crawl_wait_ms")
    final public Integer crawlWaitMs;

    protected CrawlDefinition(final Class<DEFINITION> crawldefinitionClass,
                              final @JsonProperty("crawl_collector_factory") String crawlCollectorFactoryClass,
                              final @JsonProperty("variables") LinkedHashMap<String, Object> variables,
                              final @JsonProperty("filters") LinkedHashMap<String, WildcardFilter.Status> filters,
                              final @JsonProperty("filter_policy") WildcardFilter.Status filterPolicy,
                              final @JsonProperty("max_depth") Integer maxDepth,
                              final @JsonProperty("crawl_wait_ms") Integer crawlWaitMs) {
        super(crawldefinitionClass);
        this.crawlCollectorFactoryClass = crawlCollectorFactoryClass;
        this.variables = variables == null ? null : MapUtils.copyOf(variables);
        this.filters = filters == null ? null : MapUtils.copyOf(filters);
        this.filterPolicy = filterPolicy;
        this.maxDepth = maxDepth;
        this.crawlWaitMs = crawlWaitMs;
    }

    protected CrawlDefinition(final Class<DEFINITION> crawldefinitionClass,
                              final AbstractBuilder<DEFINITION, ?> builder) {
        this(crawldefinitionClass,
                builder.crawlCollectorFactoryClass,
                builder.variables,
                builder.filters,
                builder.filterPolicy,
                builder.maxDepth,
                builder.crawlWaitMs);

    }

    @JsonIgnore
    final public String getCrawlCollectorFactoryClass() {
        return crawlCollectorFactoryClass;
    }

    final public Map<String, Object> getVariables() {
        return variables;
    }

    final public Map<String, WildcardFilter.Status> getFilters() {
        return filters;
    }

    final public Integer getMaxDepth() {
        return maxDepth;
    }

    final public Integer getCrawlWaitMs() {
        return crawlWaitMs;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(variables, filters, maxDepth, crawlWaitMs);
    }

    @Override
    protected boolean isEqual(final DEFINITION c) {
        return Objects.equals(crawlCollectorFactoryClass, c.crawlCollectorFactoryClass)
                && CollectionsUtils.equals(variables, c.variables)
                && CollectionsUtils.equals(filters, c.filters)
                && Objects.equals(maxDepth, c.maxDepth)
                && Objects.equals(crawlWaitMs, c.crawlWaitMs);
    }

    public static abstract class AbstractBuilder<
            DEFINITION extends CrawlDefinition<DEFINITION>,
            BUILDER extends AbstractBuilder<DEFINITION, BUILDER>> {

        protected String crawlCollectorFactoryClass;

        protected LinkedHashMap<String, Object> variables;

        protected LinkedHashMap<String, WildcardFilter.Status> filters;

        protected WildcardFilter.Status filterPolicy;

        protected Integer maxDepth;

        protected Integer crawlWaitMs;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(DEFINITION src) {
            variables = src.variables == null ? null : new LinkedHashMap<>(src.variables);
            crawlWaitMs = src.crawlWaitMs;
            maxDepth = src.maxDepth;
            filters = src.filters == null || src.filters.isEmpty() ? null : new LinkedHashMap<>(src.filters);
            filterPolicy = src.filterPolicy;
        }

        protected abstract BUILDER me();

        public BUILDER crawlCollectorFactoryClass(final Class<? extends CrawlCollectorFactory<?, DEFINITION>> crawlCollectorFactoryClass) {
            this.crawlCollectorFactoryClass = crawlCollectorFactoryClass.getName();
            return me();
        }

        public BUILDER variable(final String name, final String value) {
            if (variables == null)
                variables = new LinkedHashMap<>();
            variables.put(name, value);
            return me();
        }

        public BUILDER addFilter(final String filterPattern, final WildcardFilter.Status filterStatus) {
            if (StringUtils.isBlank(filterPattern) || filterStatus == null)
                return me();
            if (filters == null)
                filters = new LinkedHashMap<>();
            filters.put(filterPattern, filterStatus);
            return me();
        }

        public BUILDER setFilterPolicy(final WildcardFilter.Status filterPolicy) {
            this.filterPolicy = filterPolicy;
            return me();
        }

        public BUILDER setMaxDepth(final Integer maxDepth) {
            this.maxDepth = maxDepth;
            return me();
        }

        @JsonIgnore
        public BUILDER setCrawlWaitMs(Integer crawlWaitMs) {
            this.crawlWaitMs = crawlWaitMs;
            return me();
        }

        public abstract DEFINITION build();

    }

}
