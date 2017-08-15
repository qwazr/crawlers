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
 */
package com.qwazr.crawler.common;

public interface CurrentCrawl {

	/**
	 * @return the depth of the current URL
	 */
	Integer getDepth();

	/**
	 * @return true if the item matches an inclusion item
	 */
	Boolean isInInclusion();

	/**
	 * @return true if the item matches an exclusion item
	 */
	Boolean isInExclusion();

	/**
	 * Set the ignored flag
	 *
	 * @param ignored
	 */
	void setIgnored(boolean ignored);

	/**
	 * Check if the URL is ignored. An ignored URL is not crawled
	 *
	 * @return true if the URL is ignored
	 */
	boolean isIgnored();

	/**
	 * Check if the URL has been crawled
	 *
	 * @return true if the URL has been crawled
	 */
	boolean isCrawled();

	/**
	 * @return the error message if any
	 */
	String getError();

}