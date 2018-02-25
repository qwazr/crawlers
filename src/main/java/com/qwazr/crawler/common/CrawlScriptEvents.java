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

import com.qwazr.scripts.ScriptInterface;

import java.util.Map;

public abstract class CrawlScriptEvents<D extends CrawlDefinition, S extends CrawlSession<D, ?>, C extends CurrentCrawl>
		implements ScriptInterface {

	final static String SESSION_ATTRIBUTE = "session";
	final static String CURRENT_ATTRIBUTE = "current";

	protected final Class<S> crawlSessionClass;
	protected final Class<C> currentCrawlClass;

	protected CrawlScriptEvents(final Class<S> crawlSessionClass, final Class<C> currentCrawlClass) {
		this.crawlSessionClass = crawlSessionClass;
		this.currentCrawlClass = currentCrawlClass;
	}

	protected abstract boolean run(S session, C crawl, Map<String, ?> variables) throws Exception;

	@Override
	public boolean run(Map<String, ?> attributes) throws Exception {
		return run(crawlSessionClass.cast(attributes.get(SESSION_ATTRIBUTE)),
				currentCrawlClass.cast(attributes.get(CURRENT_ATTRIBUTE)), attributes);
	}
}
