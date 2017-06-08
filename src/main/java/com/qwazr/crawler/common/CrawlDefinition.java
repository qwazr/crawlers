/**
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
 **/
package com.qwazr.crawler.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public class CrawlDefinition implements Cloneable {

	/**
	 * The global variables shared by all the scripts.
	 */
	public LinkedHashMap<String, String> variables = null;

	/**
	 * A list of scripts paths mapped with the events which fire the scripts.
	 */
	public Map<EventEnum, Script> scripts = null;

	public enum EventEnum {

		/**
		 * Executed before the crawl session start
		 */
		before_session,

		/**
		 * Executed after the crawl session ends
		 */
		after_session,

		/**
		 * Executed before an URL is crawled
		 */
		before_crawl,

		/**
		 * Executed after an URL has been crawled
		 */
		after_crawl
	}

	@JsonInclude(Include.NON_EMPTY)
	public static class Script implements Cloneable {

		/**
		 * The path to the scripts
		 */
		public String name = null;

		/**
		 * The local variables passed to the scripts
		 */
		public Map<String, String> variables = null;

		public Script() {
		}

		public Script(String name) {
			this.name = name;
		}

		protected Script(Script src) {
			this.name = src.name;
			this.variables = src.variables == null ? null : new HashMap<String, String>(src.variables);
		}

		@Override
		final public Object clone() {
			return new Script(this);
		}

		public Script addVariable(String name, String value) {
			if (variables == null)
				variables = new HashMap<>();
			variables.put(name, value);
			return this;
		}

	}

	public CrawlDefinition() {
	}

	protected CrawlDefinition(CrawlDefinition src) {
		variables = src.variables == null ? null : new LinkedHashMap<>(src.variables);
		if (src.scripts == null) {
			scripts = null;
		} else {
			scripts = new HashMap<>();
			src.scripts.forEach((eventEnum, script) -> scripts.put(eventEnum, new Script(script)));
		}
	}

	public Object clone() {
		return new CrawlDefinition(this);
	}

	@JsonIgnore
	public Script addScript(final String event, final String name) {
		if (scripts == null)
			scripts = new LinkedHashMap<>();
		Script script = new Script(name);
		scripts.put(EventEnum.valueOf(event), script);
		return script;
	}

	public Map<EventEnum, Script> getScripts() {
		return scripts;
	}

}
