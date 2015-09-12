/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.crawler.web.manager;

import com.qwazr.crawler.web.service.WebCrawlDefinition;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CurrentSession {

	private final WebCrawlDefinition crawlDefinition;
	private final String name;
	private final Date startTime;
	private final AtomicBoolean abort;
	private final ConcurrentHashMap<String, String> variables;

	private volatile int ignoredCount = 0;
	private volatile int errorCount = 0;
	private volatile int crawledCount = 0;
	private volatile String currentURI = null;
	private volatile Integer currentDepth = null;
	private volatile String abortingReason = null;

	CurrentSession(WebCrawlDefinition crawlDefinition, String name) {
		this.crawlDefinition = crawlDefinition;
		this.name = name;
		startTime = new Date();
		abort = new AtomicBoolean(false);
		this.variables = new ConcurrentHashMap<>();
		if (crawlDefinition.variables != null)
			for (Map.Entry<String, String> entry : crawlDefinition.variables.entrySet())
				if (entry.getKey() != null && entry.getValue() != null)
					this.variables.put(entry.getKey(), entry.getValue());
	}

	/**
	 * @param name the name of the variable
	 * @return the value of the variable
	 */
	public String getVariable(String name) {
		return variables.get(name);
	}

	/**
	 * @param name  the name of the variable to set
	 * @param value the value to set
	 * @return the previous value if any
	 */
	public String setVariable(String name, String value) {
		if (value == null)
			return removeVariable(name);
		return variables.put(name, value);
	}

	/**
	 * @param name the name of the variable to remove
	 * @return the value of the variable if any
	 */
	public String removeVariable(String name) {
		return variables.remove(name);
	}

	/**
	 * Causes the currently executing thread to sleep
	 *
	 * @param millis the number of milliseconds
	 */
	public void sleep(int millis) {
		try {
			if (millis == 0)
				return;
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			abort(e.getMessage());
		}
	}

	/**
	 * Call this method to abort of the current session
	 */
	public void abort() {
		this.abort(null);
	}

	/**
	 * Call this method to abort the current session and set the reason.
	 * If it was previously aborted, the reason is not updated
	 *
	 * @param reason the motivation of the abort
	 */
	public void abort(String reason) {
		if (abort.getAndSet(true))
			return;
		abortingReason = reason;
	}

	/**
	 * Check if the session is currently aborting
	 *
	 * @return
	 */
	public boolean isAborting() {
		return abort.get();
	}

	/**
	 * @return the aborting reason if any
	 */
	public String getAbortingReason() {
		return abortingReason;
	}

	synchronized int incIgnoredCount() {
		return ignoredCount++;
	}

	/**
	 * @return The number of ignored URLs
	 */
	public Integer getIgnoredCount() {
		return ignoredCount;
	}

	synchronized int incCrawledCount() {
		return crawledCount++;
	}

	/**
	 * @return The number of crawled URLs
	 */
	public Integer getCrawledCount() {
		return crawledCount;
	}

	synchronized int incErrorCount() {
		return errorCount++;
	}

	/**
	 * @return The number of erroneous URLs
	 */
	public Integer getErrorCount() {
		return errorCount;
	}

	/**
	 * @return The time when the crawl session was started
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @return the name of the session
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the currentUri
	 */
	public String getCurrentURI() {
		return currentURI;
	}

	/**
	 * @return the currentDepth
	 */
	public Integer getCurrentDepth() {
		return currentDepth;
	}

	/**
	 * @param currentURI the currentURI to set
	 */
	synchronized public void setCurrentURI(String currentURI, Integer currentDepth) {
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
}
