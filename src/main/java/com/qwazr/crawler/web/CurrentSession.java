/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web;

import com.qwazr.utils.TimeTracker;

import java.util.Map;

public interface CurrentSession {

	Map<String, Object> getVariables();

	/**
	 * @param name the name of the variable
	 * @return the value of the variable
	 */
	Object getVariable(String name);

	/**
	 * @param name  the name of the variable to set
	 * @param value the value to set
	 * @return the previous value if any
	 */
	Object setVariable(String name, Object value);

	/**
	 * @param name the name of the variable to remove
	 * @return the value of the variable if any
	 */
	Object removeVariable(String name);

	/**
	 * Causes the currently executing thread to sleep
	 *
	 * @param millis the number of milliseconds
	 */
	void sleep(int millis);

	/**
	 * Call this method to abort of the current session
	 */
	void abort();

	/**
	 * Call this method to abort the current session and set the reason.
	 * If it was previously aborted, the reason is not updated
	 *
	 * @param reason the motivation of the abort
	 */
	void abort(String reason);

	/**
	 * Check if the session is currently aborting
	 *
	 * @return
	 */
	boolean isAborting();

	/**
	 * @return the aborting reason if any
	 */
	String getAbortingReason();

	/**
	 * @return The number of ignored URLs
	 */
	Integer getIgnoredCount();

	/**
	 * @return The number of crawled URLs
	 */
	Integer getCrawledCount();

	/**
	 * @return The number of erroneous URLs
	 */
	Integer getErrorCount();

	/**
	 * @return the name of the session
	 */
	String getName();

	/**
	 * @return the currentUri
	 */
	String getCurrentURI();

	/**
	 * @return the currentDepth
	 */
	Integer getCurrentDepth();

	/**
	 * @return time information about the current crawl session
	 */
	TimeTracker.Status getTimeTrackerStatus();
}
