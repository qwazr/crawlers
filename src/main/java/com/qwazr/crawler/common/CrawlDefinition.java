/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
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
	final public List<String> inclusionPatterns;

	/**
	 * A list of regular expression patterns. An item may not be crawled if it
	 * matches any pattern.
	 */
	@JsonProperty("exclusion_patterns")
	final public List<String> exclusionPatterns;

	protected CrawlDefinition() {
		variables = null;
		scripts = null;
		inclusionPatterns = null;
		exclusionPatterns = null;
	}

	@JsonCreator
	protected CrawlDefinition(@JsonProperty("variables") LinkedHashMap<String, String> variables,
			@JsonProperty("scripts") Map<EventEnum, ScriptDefinition> scripts,
			@JsonProperty("inclusion_patterns") List<String> inclusionPatterns,
			@JsonProperty("exclusion_patterns") List<String> exclusionPatterns) {
		this.variables = variables == null ? null : Collections.unmodifiableMap(variables);
		this.scripts = scripts == null ? null : Collections.unmodifiableMap(scripts);
		this.inclusionPatterns =
				inclusionPatterns == null ? null : Collections.unmodifiableList(new ArrayList<>(inclusionPatterns));
		this.exclusionPatterns =
				exclusionPatterns == null ? null : Collections.unmodifiableList(new ArrayList<>(exclusionPatterns));
	}

	protected CrawlDefinition(AbstractBuilder<? extends CrawlDefinition, ?> builder) {
		this(builder.variables, builder.scripts, builder.inclusionPatterns, builder.exclusionPatterns);

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
				CollectionsUtils.equals(exclusionPatterns, c.exclusionPatterns);
	}

	public static abstract class AbstractBuilder<D extends CrawlDefinition, B extends AbstractBuilder<D, B>> {

		protected LinkedHashMap<String, String> variables;

		protected Map<EventEnum, ScriptDefinition> scripts;

		private List<String> inclusionPatterns;
		private List<String> exclusionPatterns;

		protected AbstractBuilder() {
		}

		protected AbstractBuilder(D src) {
			variables = src.variables == null ? null : new LinkedHashMap<>(src.variables);
			scripts = src.scripts == null ? null : new LinkedHashMap<>(src.scripts);
			this.inclusionPatterns = src.inclusionPatterns == null ? null : new ArrayList<>(src.inclusionPatterns);
			this.exclusionPatterns = src.exclusionPatterns == null ? null : new ArrayList<>(src.exclusionPatterns);
		}

		public B variable(final String name, final String value) {
			if (variables == null)
				variables = new LinkedHashMap<>();
			variables.put(name, value);
			return (B) this;
		}

		public B script(final EventEnum event, final ScriptDefinition script) {
			if (scripts == null)
				scripts = new LinkedHashMap<>();
			scripts.put(event, script);
			return (B) this;
		}

		public B addInclusionPattern(final String inclusionPattern) {
			if (this.inclusionPatterns == null)
				this.inclusionPatterns = new ArrayList<>();
			this.inclusionPatterns.add(inclusionPattern);
			return (B) this;
		}

		public B setInclusionPattern(final String inclusionPatternText) throws IOException {
			if (inclusionPatternText == null) {
				this.inclusionPatterns = null;
				return (B) this;
			}
			this.inclusionPatterns = new ArrayList<>();
			StringUtils.linesCollector(inclusionPatternText, false, this.inclusionPatterns);
			return (B) this;
		}

		public B setExclusionPattern(final String exclusionPatternText) throws IOException {
			if (exclusionPatternText == null) {
				this.exclusionPatterns = null;
				return (B) this;
			}
			this.exclusionPatterns = new ArrayList<>();
			StringUtils.linesCollector(exclusionPatternText, false, this.exclusionPatterns);
			return (B) this;
		}

		public B addExclusionPattern(final String exclusionPattern) {
			if (this.exclusionPatterns == null)
				this.exclusionPatterns = new ArrayList<>();
			this.exclusionPatterns.add(exclusionPattern);
			return (B) this;
		}

		public abstract D build();

	}
}
