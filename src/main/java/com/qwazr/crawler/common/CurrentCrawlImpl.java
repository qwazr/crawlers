/*
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
 */
package com.qwazr.crawler.common;

import org.apache.commons.lang3.StringUtils;

public class CurrentCrawlImpl implements CurrentCrawl {

	final private Integer depth;

	private volatile boolean isIgnored = false;
	private volatile boolean isCrawled = false;
	private volatile Boolean isInInclusion = null;
	private volatile Boolean isInExclusion = null;

	private volatile String error = null;

	protected CurrentCrawlImpl(Integer depth) {
		this.depth = depth;
	}

	@Override
	public Integer getDepth() {
		return depth;
	}

	void setInInclusion(Boolean isInInclusion) {
		this.isInInclusion = isInInclusion;
	}

	@Override
	public Boolean isInInclusion() {
		return isInInclusion;
	}

	public void setInExclusion(Boolean isInExclusion) {
		this.isInExclusion = isInExclusion;
	}

	@Override
	public Boolean isInExclusion() {
		return isInExclusion;
	}

	@Override
	public void setIgnored() {
		if (isCrawled || isIgnored)
			return;
		isIgnored = true;
	}

	@Override
	public boolean isIgnored() {
		return isIgnored;
	}

	protected void setCrawled(boolean crawled) {
		if (isIgnored)
			isIgnored = false;
		this.isCrawled = crawled;
	}

	@Override
	public boolean isCrawled() {
		return isCrawled;
	}

	protected void setError(String error) {
		this.error = error;
	}

	protected void setError(Exception e) {
		if (e == null) {
			error = null;
			return;
		}
		if (StringUtils.isEmpty(error))
			error = e.toString();
		if (StringUtils.isEmpty(error))
			error = e.getClass().getName();
	}

	@Override
	public String getError() {
		return error;
	}

}
