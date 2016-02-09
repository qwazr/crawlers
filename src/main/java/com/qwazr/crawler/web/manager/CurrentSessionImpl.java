/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web.manager;

import com.qwazr.crawler.web.CurrentSession;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.utils.TimeTracker;
import org.apache.commons.collections.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class CurrentSessionImpl implements CurrentSession {

	private final WebCrawlDefinition crawlDefinition;
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

	CurrentSessionImpl(WebCrawlDefinition crawlDefinition, String name, TimeTracker timeTracker) {
		this.crawlDefinition = crawlDefinition;
		this.timeTracker = timeTracker;
		this.name = name;
		abort = new AtomicBoolean(false);
		this.variables = new ConcurrentHashMap<>();
		if (crawlDefinition.variables != null)
			for (Map.Entry<String, String> entry : crawlDefinition.variables.entrySet())
				if (entry.getKey() != null && entry.getValue() != null)
					this.variables.put(entry.getKey(), entry.getValue());
	}

	@Override
	public Map<String, Object> getVariables() {
		return variables;
	}

	@Override
	public Object getVariable(String name) {
		return variables.get(name);
	}

	@Override
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

	synchronized int incIgnoredCount() {
		return ignoredCount++;
	}

	@Override
	public Integer getIgnoredCount() {
		return ignoredCount;
	}

	synchronized int incCrawledCount() {
		return crawledCount++;
	}

	@Override
	public Integer getCrawledCount() {
		return crawledCount;
	}

	synchronized int incErrorCount() {
		return errorCount++;
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

	synchronized void setCurrentURI(String currentURI, Integer currentDepth) {
		this.currentURI = currentURI;
		this.currentDepth = currentDepth;
	}

	public WebCrawlDefinition getCrawlDefinition() {
		return crawlDefinition;
	}

	public boolean isURLPatterns() {
		return crawlDefinition != null && !CollectionUtils.isEmpty(crawlDefinition.inclusion_patterns) &&
				!CollectionUtils.isEmpty(crawlDefinition.exclusion_patterns);
	}

	public TimeTracker.Status getTimeTrackerStatus() {
		return timeTracker == null ? null : timeTracker.getStatus();
	}
}
