/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.WildcardMatcher;
import org.apache.commons.io.IOCase;

interface RobotsTxtPathMatcher {

	boolean match(String path);

	String getPattern();

	static RobotsTxtPathMatcher of(String pattern) {
		if (pattern == null)
			return null;
		final boolean isWildcard = pattern.indexOf('*') != -1;
		final boolean isEnding = pattern.endsWith("$");
		if (isEnding)
			return isWildcard ? new WildcarsdMatcher(pattern, true) : new ExactMatcher(pattern, true);
		else
			return isWildcard ? new WildcarsdMatcher(pattern, false) : new StartsWithMatcher(pattern);

	}

	abstract class PathMatcher implements RobotsTxtPathMatcher {

		final String pattern;

		PathMatcher(String pattern) {
			this.pattern = pattern;
		}

		@Override
		final public String getPattern() {
			return pattern;
		}

	}

	final class WildcarsdMatcher extends PathMatcher {

		private final WildcardMatcher matcher;

		WildcarsdMatcher(String pattern, boolean isEnding) {
			super(pattern);
			final String wPattern = isEnding ? pattern.substring(0, pattern.length() - 1) : pattern.endsWith("*") ?
					pattern :
					pattern + '*';
			matcher = new WildcardMatcher(wPattern);
		}

		@Override
		final public boolean match(String path) {
			return path != null && matcher.match(path, IOCase.SENSITIVE);
		}
	}

	final class ExactMatcher extends PathMatcher {

		private String exactPattern;

		ExactMatcher(String pattern, boolean isEnding) {
			super(pattern);
			exactPattern = isEnding ? pattern.substring(0, pattern.length() - 1) : pattern;
		}

		@Override
		final public boolean match(final String path) {
			return path != null && exactPattern.equals(path);
		}
	}

	final class StartsWithMatcher extends PathMatcher {

		StartsWithMatcher(String pattern) {
			super(pattern);
		}

		@Override
		final public boolean match(final String path) {
			return path != null && path.startsWith(pattern);
		}
	}

}
