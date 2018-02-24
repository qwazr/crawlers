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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrawlSessionImpl<D extends CrawlDefinition, S extends CrawlStatus<D>> implements CrawlSession<D, S> {

	private final D crawlDefinition;
	private final String name;
	private final AtomicBoolean abort;
	private final TimeTracker timeTracker;
	private final ConcurrentHashMap<String, Object> attributes;

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
		this.attributes = new ConcurrentHashMap<>();
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

	@Override
	public <V> V getVariable(final String name, final Class<? extends V> variableClass) {
		return crawlDefinition != null && crawlDefinition.variables != null ?
				variableClass.cast(crawlDefinition.variables.get(name)) :
				null;
	}

	@Override
	public <A> A getAttribute(final String name, final Class<? extends A> attributeClass) {
		return attributeClass.cast(attributes.get(name));
	}

	/**
	 * @param name the name of the variable
	 * @return the value of the variable
	 */
	@Override
	public <A> A setAttribute(final String name, final A attribute, final Class<? extends A> attributeClass) {
		if (attribute == null)
			return removeAttribute(name, attributeClass);
		return attributeClass.cast(attributes.put(name, attribute));
	}

	@Override
	public <A> A removeAttribute(final String name, final Class<? extends A> attributeClass) {
		return attributeClass.cast(attributes.remove(name));
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

	void done() {
		crawlStatusBuilder.done();
		buildStatus();
	}

	void setFuture(Future<?> future) {
		crawlStatusBuilder.future(future);
		buildStatus();
	}
}
