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

	private volatile int ignoredCount = 0;
	private volatile int errorCount = 0;
	private volatile int crawledCount = 0;
	private volatile String currentURI = null;
	private volatile Integer currentDepth = null;
	private volatile String abortingReason = null;

	public CrawlSessionImpl(T crawlDefinition, String name) {
		this.crawlDefinition = crawlDefinition;
		this.timeTracker = new TimeTracker();
		this.name = name;
		abort = new AtomicBoolean(false);
		this.variables = new ConcurrentHashMap<>();
		if (crawlDefinition.variables != null) {
			crawlDefinition.variables.forEach((key, value) -> {
				if (key != null && value != null)
					this.variables.put(key, value);
			});
		}
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
	public void abort() {
		this.abort(null);
	}

	@Override
	public void abort(String reason) {
		if (abort.getAndSet(true))
			return;
		abortingReason = reason;
	}

	@Override
	public boolean isAborting() {
		return abort.get();
	}

	@Override
	public String getAbortingReason() {
		return abortingReason;
	}

	public synchronized int incIgnoredCount() {
		return ++ignoredCount;
	}

	@Override
	public Integer getIgnoredCount() {
		return ignoredCount;
	}

	public synchronized int incCrawledCount() {
		return ++crawledCount;
	}

	@Override
	public Integer getCrawledCount() {
		return crawledCount;
	}

	public synchronized int incErrorCount() {
		return ++errorCount;
	}

	@Override
	public Integer getErrorCount() {
		return errorCount;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getCurrentURI() {
		return currentURI;
	}

	@Override
	public Integer getCurrentDepth() {
		return currentDepth;
	}

	public synchronized void setCurrentURI(String currentURI, Integer currentDepth) {
		this.currentURI = currentURI;
		this.currentDepth = currentDepth;
	}

	public T getCrawlDefinition() {
		return crawlDefinition;
	}

	public TimeTracker getTimeTracker() {
		return timeTracker;
	}
}
