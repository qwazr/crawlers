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
 **/
package com.qwazr.crawler.common;

import com.qwazr.utils.ExceptionUtils;
import com.qwazr.utils.TimeTracker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrawlSessionImpl<T extends CrawlDefinition> implements CrawlSession {

	private final T crawlDefinition;
	private final String name;
	private final AtomicBoolean abort;
	private final TimeTracker timeTracker;
	private final ConcurrentHashMap<String, Object> variables;

	private volatile CrawlStatus<T> crawlStatus;
	private final CrawlStatus.AbstractBuilder<T> crawlStatusBuilder;

	public CrawlSessionImpl(String sessionName, TimeTracker timeTracker, T crawlDefinition,
			CrawlStatus.AbstractBuilder<T> crawlStatusBuilder) {
		this.timeTracker = timeTracker;
		this.crawlStatusBuilder = crawlStatusBuilder;
		this.crawlStatus = crawlStatusBuilder.build();
		this.crawlDefinition = crawlDefinition;
		this.name = sessionName;
		abort = new AtomicBoolean(false);
		this.variables = new ConcurrentHashMap<>();
		if (crawlDefinition.variables != null) {
			crawlDefinition.variables.forEach((key, value) -> {
				if (key != null && value != null)
					this.variables.put(key, value);
			});
		}
	}

	@Override
	public CrawlStatus getCrawlStatus() {
		return crawlStatus;
	}

	public Map<String, Object> getVariables() {
		return variables;
	}

	@Override
	public Object getVariable(String name) {
		return variables.get(name);
	}

	/**
	 * @param name the name of the variable
	 * @return the value of the variable
	 */
	public Object setVariable(String name, Object value) {
		if (value == null)
			return removeVariable(name);
		return variables.put(name, value);
	}

	@Override
	public Object removeVariable(String name) {
		return variables.remove(name);
	}

	@Override
	public void sleep(int millis) {
		try {
			if (millis == 0)
				return;
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			abort(e.getMessage());
		}
	}

	@Override
	public void abort(String reason) {
		if (abort.getAndSet(true))
			return;
		crawlStatus = crawlStatusBuilder.abort(reason).build();
	}

	@Override
	public boolean isAborting() {
		return abort.get();
	}

	public void incIgnoredCount() {
		crawlStatus = crawlStatusBuilder.incIgnored().build();
	}

	public int incCrawledCount() {
		return (crawlStatus = crawlStatusBuilder.incCrawled().build()).crawled;
	}

	public void incErrorCount(String errorMessage) {
		crawlStatus = crawlStatusBuilder.lastError(errorMessage).incError().build();
	}

	public void error(Exception e) {
		crawlStatus = crawlStatusBuilder.lastError(ExceptionUtils.getRootCauseMessage(e)).build();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setCurrentCrawl(String currentCrawl, Integer currentDepth) {
		crawlStatus = crawlStatusBuilder.crawl(currentCrawl, currentDepth).build();
	}

	public T getCrawlDefinition() {
		return crawlDefinition;
	}

	public TimeTracker getTimeTracker() {
		return timeTracker;
	}

	public void done() {
		crawlStatus = crawlStatusBuilder.done().build();
	}
}
