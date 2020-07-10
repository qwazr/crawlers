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
import com.qwazr.utils.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
     * A list of regular expression patterns. An item may be crawled only if it
     * matches any pattern.
     */
    @JsonProperty("inclusion_patterns")
    final public Collection<String> inclusionPatterns;

    /**
     * A list of regular expression patterns. An item may not be crawled if it
     * matches any pattern.
     */
    @JsonProperty("exclusion_patterns")
    final public Collection<String> exclusionPatterns;

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
                              final @JsonProperty("inclusion_patterns") Collection<String> inclusionPatterns,
                              final @JsonProperty("exclusion_patterns") Collection<String> exclusionPatterns,
                              final @JsonProperty("max_depth") Integer maxDepth,
                              final @JsonProperty("crawl_wait_ms") Integer crawlWaitMs) {
        super(crawldefinitionClass);
        this.crawlCollectorFactoryClass = crawlCollectorFactoryClass;
        this.variables = variables == null ? null : Map.copyOf(variables);
        this.inclusionPatterns =
                inclusionPatterns == null ? null : List.copyOf(inclusionPatterns);
        this.exclusionPatterns =
                exclusionPatterns == null ? null : List.copyOf(new ArrayList<>(exclusionPatterns));
        this.maxDepth = maxDepth;
        this.crawlWaitMs = crawlWaitMs;
    }

    protected CrawlDefinition(final Class<DEFINITION> crawldefinitionClass,
                              final AbstractBuilder<DEFINITION, ?> builder) {
        this(crawldefinitionClass,
                builder.crawlCollectorFactoryClass,
                builder.variables,
                builder.inclusionPatterns,
                builder.exclusionPatterns,
                builder.maxDepth,
                builder.crawlWaitMs);

    }

    final public String getCrawlCollectorFactoryClass() {
        return crawlCollectorFactoryClass;
    }

    final public Map<String, Object> getVariables() {
        return variables;
    }

    final public Collection<String> getInclusionPatterns() {
        return inclusionPatterns;
    }

    final public Collection<String> getExclusionPatterns() {
        return exclusionPatterns;
    }

    final public Integer getMaxDepth() {
        return maxDepth;
    }

    final public Integer getCrawlWaitMs() {
        return crawlWaitMs;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(variables, inclusionPatterns, exclusionPatterns, maxDepth, crawlWaitMs);
    }

    @Override
    protected boolean isEqual(final DEFINITION c) {
        return Objects.equals(crawlCollectorFactoryClass, c.crawlCollectorFactoryClass)
                && CollectionsUtils.equals(variables, c.variables)
                && CollectionsUtils.equals(inclusionPatterns, c.inclusionPatterns)
                && CollectionsUtils.equals(exclusionPatterns, c.exclusionPatterns)
                && Objects.equals(maxDepth, c.maxDepth)
                && Objects.equals(crawlWaitMs, c.crawlWaitMs);
    }

    public static abstract class AbstractBuilder<
            DEFINITION extends CrawlDefinition<DEFINITION>,
            BUILDER extends AbstractBuilder<DEFINITION, BUILDER>> {

        protected String crawlCollectorFactoryClass;

        protected LinkedHashMap<String, Object> variables;

        protected LinkedHashSet<String> inclusionPatterns;
        protected LinkedHashSet<String> exclusionPatterns;

        protected Integer maxDepth;

        protected Integer crawlWaitMs;

        private final Class<BUILDER> builderClass;

        protected AbstractBuilder(final Class<BUILDER> builderClass) {
            this.builderClass = builderClass;
        }

        protected AbstractBuilder(final Class<BUILDER> builderClass, DEFINITION src) {
            this(builderClass);
            variables = src.variables == null ? null : new LinkedHashMap<>(src.variables);
            inclusionPatterns = src.inclusionPatterns == null || src.inclusionPatterns.isEmpty() ?
                    null :
                    new LinkedHashSet<>(src.inclusionPatterns);
            exclusionPatterns = src.exclusionPatterns == null || src.exclusionPatterns.isEmpty() ?
                    null :
                    new LinkedHashSet<>(src.exclusionPatterns);
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

        public BUILDER setInclusionPatterns(final Collection<String> inclusionPatterns) {
            this.inclusionPatterns = inclusionPatterns == null || inclusionPatterns.isEmpty() ?
                    null :
                    new LinkedHashSet<>(inclusionPatterns);
            return me();
        }

        public BUILDER setInclusionPatterns(final String inclusionPatternText) throws IOException {
            if (StringUtils.isBlank(inclusionPatternText)) {
                inclusionPatterns = null;
                return builderClass.cast(this);
            }
            if (inclusionPatterns != null)
                inclusionPatterns.clear();
            else
                inclusionPatterns = new LinkedHashSet<>();
            StringUtils.linesCollector(inclusionPatternText, false, inclusionPatterns);
            return me();
        }

        public BUILDER addInclusionPattern(final String inclusionPattern) {
            if (StringUtils.isBlank(inclusionPattern))
                return builderClass.cast(this);
            if (inclusionPatterns == null)
                inclusionPatterns = new LinkedHashSet<>();
            inclusionPatterns.add(inclusionPattern);
            return me();
        }

        public BUILDER setExclusionPatterns(final Collection<String> exclusionPatterns) {
            this.exclusionPatterns = exclusionPatterns == null || exclusionPatterns.isEmpty() ?
                    null :
                    new LinkedHashSet<>(exclusionPatterns);
            return me();
        }

        public BUILDER setExclusionPatterns(final String exclusionPatternText) throws IOException {
            if (StringUtils.isBlank(exclusionPatternText)) {
                exclusionPatterns = null;
                return me();
            }
            if (exclusionPatterns != null)
                exclusionPatterns.clear();
            else
                exclusionPatterns = new LinkedHashSet<>();
            StringUtils.linesCollector(exclusionPatternText, false, exclusionPatterns);
            return me();
        }

        public BUILDER addExclusionPattern(final String exclusionPattern) {
            if (StringUtils.isBlank(exclusionPattern))
                return me();
            if (exclusionPatterns == null)
                exclusionPatterns = new LinkedHashSet<>();
            exclusionPatterns.add(exclusionPattern);
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
