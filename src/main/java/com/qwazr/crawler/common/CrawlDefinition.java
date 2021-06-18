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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public abstract class CrawlDefinition<
        DEFINITION extends CrawlDefinition<DEFINITION>
        > extends Equalizer.Immutable<DEFINITION> {

    /**
     * The name of the crawlCollectorFactory class
     */
    @JsonProperty("crawl_collector_factory")
    final public String crawlCollectorFactoryClass;

    @JsonInclude(Include.NON_EMPTY)
    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    final static public class Variable extends Equalizer<Variable> {
        @JsonProperty
        final public String key;
        @JsonProperty
        final public Object value;

        @JsonCreator
        public Variable(@JsonProperty("key") String key,
                        @JsonProperty("value") Object value) {
            super(Variable.class);
            this.key = key;
            this.value = value;
        }

        @Override
        protected boolean isEqual(Variable other) {
            return Objects.equals(key, other.key) && Objects.equals(value, other.value);
        }
    }

    /**
     * The users variables shared by all the scripts.
     */
    final public List<Variable> variables;


    @JsonInclude(Include.NON_EMPTY)
    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    final static public class Filter extends Equalizer<Filter> {
        @JsonProperty
        final public String pattern;
        @JsonProperty
        final public WildcardFilter.Status status;

        @JsonCreator
        public Filter(@JsonProperty("pattern") String pattern,
                      @JsonProperty("status") WildcardFilter.Status status) {
            super(Filter.class);
            this.pattern = pattern;
            this.status = status;
        }

        @Override
        protected boolean isEqual(Filter other) {
            return Objects.equals(pattern, other.pattern) && status == other.status;
        }
    }

    /**
     * A list of wildcard patterns. An item may be crawled only if it
     * matches any pattern with a FilterStatus sets to 'accept'.
     */
    @JsonProperty("filters")
    final public List<Filter> filters;

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
                              final @JsonProperty("variables") List<Variable> variables,
                              final @JsonProperty("filters") List<Filter> filters,
                              final @JsonProperty("filter_policy") WildcardFilter.Status filterPolicy,
                              final @JsonProperty("max_depth") Integer maxDepth,
                              final @JsonProperty("crawl_wait_ms") Integer crawlWaitMs) {
        super(crawldefinitionClass);
        this.crawlCollectorFactoryClass = crawlCollectorFactoryClass;
        this.variables = variables == null ? null : List.copyOf(variables);
        this.filters = filters == null ? null : List.copyOf(filters);
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

    final public List<Variable> getVariables() {
        return variables;
    }

    final public List<Filter> getFilters() {
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
                && Objects.deepEquals(variables, c.variables)
                && Objects.deepEquals(filters, c.filters)
                && Objects.equals(maxDepth, c.maxDepth)
                && Objects.equals(crawlWaitMs, c.crawlWaitMs);
    }

    public static abstract class AbstractBuilder<
            DEFINITION extends CrawlDefinition<DEFINITION>,
            BUILDER extends AbstractBuilder<DEFINITION, BUILDER>> {

        protected String crawlCollectorFactoryClass;

        protected List<Variable> variables;

        protected List<Filter> filters;

        protected WildcardFilter.Status filterPolicy;

        protected Integer maxDepth;

        protected Integer crawlWaitMs;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(DEFINITION src) {
            variables = src.variables == null ? null : List.copyOf(src.variables);
            crawlWaitMs = src.crawlWaitMs;
            maxDepth = src.maxDepth;
            filters = src.filters == null || src.filters.isEmpty() ? null : List.copyOf(src.filters);
            filterPolicy = src.filterPolicy;
        }

        protected abstract BUILDER me();

        public BUILDER crawlCollectorFactoryClass(final Class<? extends CrawlCollectorFactory<?, DEFINITION>> crawlCollectorFactoryClass) {
            this.crawlCollectorFactoryClass = crawlCollectorFactoryClass.getName();
            return me();
        }

        public BUILDER variable(final String name, final String value) {
            if (variables == null)
                variables = new ArrayList<>();
            variables.add(new Variable(name, value));
            return me();
        }

        public BUILDER addFilter(final String filterPattern, final WildcardFilter.Status filterStatus) {
            if (StringUtils.isBlank(filterPattern) || filterStatus == null)
                return me();
            if (filters == null)
                filters = new ArrayList<>();
            filters.add(new Filter(filterPattern, filterStatus));
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
