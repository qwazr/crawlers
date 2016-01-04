/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.qwazr.crawler.web.manager.CurrentSession;

import java.lang.Thread.State;
import java.util.Date;

@JsonInclude(Include.NON_EMPTY)
public class WebCrawlStatus {

	final public String node_address;
	final public Date start_time;
	final public State state;
	final public String entry_url;
	final public Boolean aborting;
	final public String aborting_reason;
	final public UrlStatus urls;

	public WebCrawlStatus() {
		node_address = null;
		start_time = null;
		state = null;
		entry_url = null;
		aborting = null;
		aborting_reason = null;
		urls = null;
	}

	public WebCrawlStatus(String node_address, String entry_url, State state, CurrentSession session) {
		this.node_address = node_address;
		this.entry_url = entry_url;
		this.start_time = session.getStartTime();
		this.state = state;
		this.aborting = session.isAborting();
		this.aborting_reason = session.getAbortingReason();
		this.urls = new UrlStatus(session);
	}

	@JsonInclude(Include.NON_EMPTY)
	public static class UrlStatus {

		final public int crawled;
		final public int ignored;
		final public int error;
		final public String current_uri;
		final public Integer current_depth;

		public UrlStatus() {
			crawled = 0;
			ignored = 0;
			error = 0;
			current_uri = null;
			current_depth = null;
		}

		private UrlStatus(CurrentSession session) {
			this.crawled = session.getCrawledCount();
			this.ignored = session.getIgnoredCount();
			this.error = session.getErrorCount();
			this.current_uri = session.getCurrentURI();
			this.current_depth = session.getCurrentDepth();
		}
	}
}
