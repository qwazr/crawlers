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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScriptDefinition {

	/**
	 * The path to the scripts or the full name of a class with a public empty constructor
	 */
	final public String name;

	/**
	 * The local variables passed to the scripts
	 */
	final public Map<String, String> variables;

	@JsonCreator
	ScriptDefinition(@JsonProperty("name") String name,
			@JsonProperty("variables") LinkedHashMap<String, String> variables) {
		this.name = name;
		this.variables = variables == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(variables));
	}

	public static Builder of(String name) {
		return new Builder(name);
	}

	public static Builder of(Class<?> scriptClass) {
		return new Builder(scriptClass.getName());
	}

	public static class Builder {

		private final String name;

		private LinkedHashMap<String, String> variables;

		public Builder(String name) {
			this.name = name;
		}

		public Builder variable(String name, String value) {
			if (variables == null)
				variables = new LinkedHashMap<>();
			variables.put(name, value);
			return this;
		}

		public ScriptDefinition build() {
			return new ScriptDefinition(name, variables);
		}
	}

}

