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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Contains the clause list of a "robots.txt" file for one "User-agent".
 */
public final class RobotsTxtClauseSet {

	private final static RobotsTxtClauseSet EMPTY = new RobotsTxtClauseSet();

	private final Map<RobotsTxtPathMatcher, Boolean> clauses;

	private RobotsTxtClauseSet(Builder builder) {
		clauses = Collections.unmodifiableMap(builder.clauses);
	}

	private RobotsTxtClauseSet() {
		clauses = null;
	}

	/**
	 * @param path the path to check
	 * @return false if the URL is not allowed
	 */
	final boolean isAllowed(String path) {
		if (clauses == null || path == null)
			return true;
		final String fpath = path.isEmpty() ? path + '/' : path;
		final AtomicReference<String> pattern = new AtomicReference<>();
		final AtomicBoolean status = new AtomicBoolean(true);
		clauses.forEach((matcher, st) -> {
			if (matcher.match(fpath)) {
				final String pt = pattern.get();
				if (pt != null && pt.length() >= matcher.getPattern().length())
					return;
				pattern.set(matcher.getPattern());
				status.set(st);
			}
		});
		return status.get();
	}

	public Map<RobotsTxtPathMatcher, Boolean> getClauses() {
		return clauses;
	}

	static Builder of() {
		return new Builder();
	}

	final static class Builder {

		private Map<RobotsTxtPathMatcher, Boolean> clauses;

		private void add(final RobotsTxtPathMatcher matcher, final Boolean result) {
			if (matcher == null)
				return;
			if (clauses == null)
				clauses = new LinkedHashMap<>();
			clauses.put(matcher, result);
		}

		/**
		 * Add an allow clause
		 *
		 * @param pattern the path of the clause
		 */
		final void allow(String pattern) {
			add(RobotsTxtPathMatcher.of(pattern), true);
		}

		final void disallow(String pattern) {
			add(RobotsTxtPathMatcher.of(pattern), false);
		}

		final RobotsTxtClauseSet build() {
			return clauses == null || clauses.isEmpty() ? EMPTY : new RobotsTxtClauseSet(this);
		}

	}
}
