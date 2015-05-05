/**   
 * License Agreement for QWAZR
 *
 * Copyright (C) 2014-2015 OpenSearchServer Inc.
 * 
 * http://www.qwazr.com
 * 
 * This file is part of QWAZR.
 *
 * QWAZR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * QWAZR is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QWAZR. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.qwazr.crawler.web.manager;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CurrentSession {

	private final String name;
	private final Date startTime;
	private final AtomicBoolean abort;
	private final ConcurrentHashMap<String, String> variables;

	private volatile int ignoredCount = 0;
	private volatile int errorCount = 0;
	private volatile int crawledCount = 0;
	private volatile String currentUri = null;

	CurrentSession(String name, Map<String, String> variables) {
		this.name = name;
		startTime = new Date();
		abort = new AtomicBoolean(false);
		this.variables = new ConcurrentHashMap<>();
		if (variables != null)
			this.variables.putAll(variables);
	}

	/**
	 * @param name
	 *            the name of the variable
	 * @return the value of the variable
	 */
	public String getVariable(String name) {
		return variables.get(name);
	}

	/**
	 * @param name
	 *            the name of the variable to set
	 * @param value
	 *            the value to set
	 * @return the previous value if any
	 */
	public String setVariable(String name, String value) {
		if (value == null)
			return removeVariable(name);
		return variables.put(name, value);
	}

	/**
	 * @param name
	 *            the name of the variable to remove
	 * @return the value of the variable if any
	 */
	public String removeVariable(String name) {
		return variables.remove(name);
	}

	/**
	 * Causes the currently executing thread to sleep
	 * 
	 * @param millis
	 *            the number of milliseconds
	 */
	public void sleep(int millis) {
		try {
			if (millis == 0)
				return;
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			abort();
		}
	}

	/**
	 * Call this method to request the aborting of the current session
	 */
	public void abort() {
		abort.set(true);
	}

	/**
	 * Check if the session is currently aborting
	 * 
	 * @return
	 */
	public boolean isAborting() {
		return abort.get();
	}

	void incIgnoredCount() {
		ignoredCount++;
	}

	/**
	 * @return The number of ignored URLs
	 */
	public Integer getIgnoredCount() {
		return ignoredCount;
	}

	void incCrawledCount() {
		crawledCount++;
	}

	/**
	 * @return The number of crawled URLs
	 */
	public Integer getCrawledCount() {
		return crawledCount;
	}

	void incErrorCount() {
		errorCount++;
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
	public String getCurrentUri() {
		return currentUri;
	}

	/**
	 * @param currentUri
	 *            the currentUri to set
	 */
	public void setCurrentUri(String currentUri) {
		this.currentUri = currentUri;
	}
}
