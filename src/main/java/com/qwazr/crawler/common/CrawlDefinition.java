/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		creatorVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE,
		fieldVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
public abstract class CrawlDefinition {

	/**
	 * The global variables shared by all the scripts.
	 */
	final public Map<String, String> variables;

	/**
	 * A list of scripts paths mapped with the events which fire the scripts.
	 */
	final public Map<EventEnum, ScriptDefinition> scripts;

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
	 * Time wait on successfull crawl
	 */
	@JsonProperty("crawl_wait_ms")
	final public Integer crawlWaitMs;

	@JsonCreator
	protected CrawlDefinition(@JsonProperty("variables") LinkedHashMap<String, String> variables,
			@JsonProperty("scripts") Map<EventEnum, ScriptDefinition> scripts,
			@JsonProperty("inclusion_patterns") Collection<String> inclusionPatterns,
			@JsonProperty("exclusion_patterns") Collection<String> exclusionPatterns,
			@JsonProperty("max_depth") Integer maxDepth, @JsonProperty("crawl_wait_ms") Integer crawlWaitMs) {
		this.variables = variables == null ? null : Collections.unmodifiableMap(variables);
		this.scripts = scripts == null ? null : Collections.unmodifiableMap(scripts);
		this.inclusionPatterns =
				inclusionPatterns == null ? null : Collections.unmodifiableList(new ArrayList<>(inclusionPatterns));
		this.exclusionPatterns =
				exclusionPatterns == null ? null : Collections.unmodifiableList(new ArrayList<>(exclusionPatterns));
		this.maxDepth = maxDepth;
		this.crawlWaitMs = crawlWaitMs;
	}

	protected CrawlDefinition(AbstractBuilder<? extends CrawlDefinition, ?> builder) {
		this(builder.variables, builder.scripts, builder.inclusionPatterns, builder.exclusionPatterns, builder.maxDepth,
				builder.crawlWaitMs);

	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public Map<EventEnum, ScriptDefinition> getScripts() {
		return scripts;
	}

	@JsonIgnore
	public Collection<String> getInclusionPatterns() {
		return inclusionPatterns;
	}

	@JsonIgnore
	public Collection<String> getExclusionPatterns() {
		return exclusionPatterns;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof CrawlDefinition))
			return false;
		if (o == this)
			return true;
		final CrawlDefinition c = (CrawlDefinition) o;
		return CollectionsUtils.equals(variables, c.variables) && CollectionsUtils.equals(scripts, c.scripts) &&
				CollectionsUtils.equals(inclusionPatterns, c.inclusionPatterns) &&
				CollectionsUtils.equals(exclusionPatterns, c.exclusionPatterns) &&
				Objects.equals(maxDepth, c.maxDepth) && Objects.equals(crawlWaitMs, c.crawlWaitMs);
	}

	public static abstract class AbstractBuilder<D extends CrawlDefinition, B extends AbstractBuilder<D, B>> {

		protected LinkedHashMap<String, String> variables;

		protected Map<EventEnum, ScriptDefinition> scripts;

		protected LinkedHashSet<String> inclusionPatterns;
		protected LinkedHashSet<String> exclusionPatterns;

		protected Integer maxDepth;

		protected Integer crawlWaitMs;

		private final Class<B> builderClass;

		protected AbstractBuilder(final Class<B> builderClass) {
			this.builderClass = builderClass;
		}

		protected AbstractBuilder(final Class<B> builderClass, D src) {
			this(builderClass);
			variables = src.variables == null ? null : new LinkedHashMap<>(src.variables);
			scripts = src.scripts == null ? null : new LinkedHashMap<>(src.scripts);
			inclusionPatterns = src.inclusionPatterns == null || src.inclusionPatterns.isEmpty() ?
					null :
					new LinkedHashSet<>(src.inclusionPatterns);
			exclusionPatterns = src.exclusionPatterns == null || src.exclusionPatterns.isEmpty() ?
					null :
					new LinkedHashSet<>(src.exclusionPatterns);
		}

		public B variable(final String name, final String value) {
			if (variables == null)
				variables = new LinkedHashMap<>();
			variables.put(name, value);
			return builderClass.cast(this);
		}

		public B script(final EventEnum event, final ScriptDefinition script) {
			if (scripts == null)
				scripts = new LinkedHashMap<>();
			scripts.put(event, script);
			return builderClass.cast(this);
		}

		public B setInclusionPatterns(final Collection<String> inclusionPatterns) {
			this.inclusionPatterns = inclusionPatterns == null || inclusionPatterns.isEmpty() ?
					null :
					new LinkedHashSet<>(inclusionPatterns);
			return builderClass.cast(this);
		}

		public B setInclusionPatterns(final String inclusionPatternText) throws IOException {
			if (StringUtils.isBlank(inclusionPatternText)) {
				inclusionPatterns = null;
				return builderClass.cast(this);
			}
			if (inclusionPatterns != null)
				inclusionPatterns.clear();
			else
				inclusionPatterns = new LinkedHashSet<>();
			StringUtils.linesCollector(inclusionPatternText, false, inclusionPatterns);
			return builderClass.cast(this);
		}

		public B addInclusionPattern(final String inclusionPattern) {
			if (StringUtils.isBlank(inclusionPattern))
				return builderClass.cast(this);
			if (inclusionPatterns == null)
				inclusionPatterns = new LinkedHashSet<>();
			inclusionPatterns.add(inclusionPattern);
			return builderClass.cast(this);
		}

		public B setExclusionPatterns(final Collection<String> exclusionPatterns) {
			this.exclusionPatterns = exclusionPatterns == null || exclusionPatterns.isEmpty() ?
					null :
					new LinkedHashSet<>(exclusionPatterns);
			return builderClass.cast(this);
		}

		public B setExclusionPatterns(final String exclusionPatternText) throws IOException {
			if (StringUtils.isBlank(exclusionPatternText)) {
				exclusionPatterns = null;
				return builderClass.cast(this);
			}
			if (exclusionPatterns != null)
				exclusionPatterns.clear();
			else
				exclusionPatterns = new LinkedHashSet<>();
			StringUtils.linesCollector(exclusionPatternText, false, exclusionPatterns);
			return builderClass.cast(this);
		}

		public B addExclusionPattern(final String exclusionPattern) {
			if (StringUtils.isBlank(exclusionPattern))
				return builderClass.cast(this);
			if (exclusionPatterns == null)
				exclusionPatterns = new LinkedHashSet<>();
			exclusionPatterns.add(exclusionPattern);
			return builderClass.cast(this);
		}

		public B setMaxDepth(final Integer maxDepth) {
			this.maxDepth = maxDepth;
			return builderClass.cast(this);
		}

		@JsonIgnore
		public B setCrawlWaitMs(Integer crawlWaitMs) {
			this.crawlWaitMs = crawlWaitMs;
			return builderClass.cast(this);
		}

		public abstract D build();

	}
}
