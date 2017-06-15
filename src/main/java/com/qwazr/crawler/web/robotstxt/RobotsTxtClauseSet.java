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
package com.qwazr.crawler.web.robotstxt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contains the clause list of a "robots.txt" file for one "User-agent".
 */
final class RobotsTxtClauseSet {

	private final static RobotsTxtClauseSet EMPTY = new RobotsTxtClauseSet();

	private final Map<RobotsTxtPathMatcher, Boolean> clauses;
	private final boolean defaultValue;

	private RobotsTxtClauseSet(Builder builder) {
		clauses = Collections.unmodifiableMap(builder.clauses);
		defaultValue = builder.defautValue;
	}

	private RobotsTxtClauseSet() {
		clauses = null;
		defaultValue = true;
	}

	/**
	 * @param path the path to check
	 * @return false if the URL is not allowed
	 */
	final boolean isAllowed(String path) {
		if (clauses == null || path == null)
			return true;
		if (path.isEmpty())
			path = "/";
		for (Map.Entry<RobotsTxtPathMatcher, Boolean> entry : clauses.entrySet())
			if (entry.getKey().match(path))
				return entry.getValue();
		return true;
	}

	/**
	 * If there is any allow entry, the default value if false
	 *
	 * @return a default value
	 */
	final boolean getDefaultValue() {
		return defaultValue;
	}

	static Builder of() {
		return new Builder();
	}

	final static class Builder {

		private Map<RobotsTxtPathMatcher, Boolean> clauses;
		private boolean defautValue = true;

		private boolean add(final RobotsTxtPathMatcher matcher, final Boolean result) {
			if (matcher == null)
				return false;
			if (clauses == null)
				clauses = new LinkedHashMap<>();
			clauses.put(matcher, result);
			return true;
		}

		/**
		 * Add an allow clause
		 *
		 * @param pattern the path of the clause
		 */
		final void allow(String pattern) {
			if (add(RobotsTxtPathMatcher.of(pattern), true))
				defautValue = false; // If we have any allow clause, the default value is false
		}

		final void disallow(String pattern) {
			add(RobotsTxtPathMatcher.of(pattern), false);
		}

		final RobotsTxtClauseSet build() {
			return clauses == null || clauses.isEmpty() ? EMPTY : new RobotsTxtClauseSet(this);
		}

	}
}
