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
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrawlSessionImpl<D extends CrawlDefinition, S extends CrawlStatus<D>> implements CrawlSession {

	private final D crawlDefinition;
	private final String name;
	private final AtomicBoolean abort;
	private final TimeTracker timeTracker;
	private final ConcurrentHashMap<String, Object> variables;

	private volatile S crawlStatusNoDefinition;
	private volatile S crawlStatusWithDefinition;
	private final CrawlStatus.AbstractBuilder<D, S, ?> crawlStatusBuilder;

	public CrawlSessionImpl(String sessionName, TimeTracker timeTracker, D crawlDefinition,
			CrawlStatus.AbstractBuilder<D, S, ?> crawlStatusBuilder) {
		this.timeTracker = timeTracker;
		this.crawlStatusBuilder = crawlStatusBuilder;
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
		buildStatus();
	}

	private void buildStatus() {
		crawlStatusWithDefinition = crawlStatusBuilder.build(true);
		crawlStatusNoDefinition = crawlStatusBuilder.build(false);
	}

	@Override
	public S getCrawlStatus(boolean withDefinition) {
		return withDefinition ? crawlStatusWithDefinition : crawlStatusNoDefinition;
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
		crawlStatusBuilder.abort(reason);
		buildStatus();
	}

	@Override
	public boolean isAborting() {
		return abort.get();
	}

	public void incIgnoredCount() {
		crawlStatusBuilder.incIgnored();
		buildStatus();
	}

	public int incCrawledCount() {
		crawlStatusBuilder.incCrawled();
		buildStatus();
		return crawlStatusNoDefinition.crawled;
	}

	public void incRedirectCount() {
		crawlStatusBuilder.incRedirect();
		buildStatus();
	}

	public void incErrorCount(String errorMessage) {
		crawlStatusBuilder.lastError(errorMessage).incError();
		buildStatus();
	}

	public void error(Exception e) {
		crawlStatusBuilder.lastError(ExceptionUtils.getRootCauseMessage(e));
		buildStatus();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setCurrentCrawl(String currentCrawl, Integer currentDepth) {
		crawlStatusBuilder.crawl(currentCrawl, currentDepth);
		buildStatus();
	}

	public D getCrawlDefinition() {
		return crawlDefinition;
	}

	public TimeTracker getTimeTracker() {
		return timeTracker;
	}

	public void done() {
		crawlStatusBuilder.done();
		buildStatus();
	}

}
