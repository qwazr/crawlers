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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.CollectionsUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
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

	protected CrawlDefinition() {
		variables = null;
		scripts = null;
	}

	@JsonCreator
	protected CrawlDefinition(@JsonProperty("variables") LinkedHashMap<String, String> variables,
			@JsonProperty("scripts") Map<EventEnum, ScriptDefinition> scripts) {
		this.variables = variables == null ? null : Collections.unmodifiableMap(variables);
		this.scripts = scripts == null ? null : Collections.unmodifiableMap(scripts);
	}

	protected CrawlDefinition(AbstractBuilder<? extends CrawlDefinition, ?> builder) {
		this(builder.variables, builder.scripts);
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public Map<EventEnum, ScriptDefinition> getScripts() {
		return scripts;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof CrawlDefinition))
			return false;
		if (o == this)
			return true;
		final CrawlDefinition c = (CrawlDefinition) o;
		return CollectionsUtils.equals(variables, c.variables) && CollectionsUtils.equals(scripts, c.scripts);
	}

	public static abstract class AbstractBuilder<D extends CrawlDefinition, B extends AbstractBuilder<D, ?>> {

		protected LinkedHashMap<String, String> variables;

		protected Map<EventEnum, ScriptDefinition> scripts;

		protected AbstractBuilder() {
		}

		protected AbstractBuilder(D crawlDefinition) {
			variables = crawlDefinition.variables == null ? null : new LinkedHashMap<>(crawlDefinition.variables);
			scripts = crawlDefinition.scripts == null ? null : new LinkedHashMap<>(crawlDefinition.scripts);
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

		public abstract D build();

	}
}
