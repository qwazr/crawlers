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
	final public UrlStatus urls;

	public WebCrawlStatus() {
		this(null, null, null, null, null);
	}

	public WebCrawlStatus(String node_address, String entry_url,
						  Date start_time, State state, UrlStatus urls) {
		this.node_address = node_address;
		this.entry_url = entry_url;
		this.start_time = start_time;
		this.state = state;
		this.urls = urls;
	}

	@JsonInclude(Include.NON_EMPTY)
	public static class UrlStatus {

		final public int crawled;
		final public int ignored;
		final public int error;
		final public String current_uri;

		public UrlStatus() {
			crawled = 0;
			ignored = 0;
			error = 0;
			current_uri = null;
		}

		public UrlStatus(CurrentSession session) {
			this.crawled = session.getCrawledCount();
			this.ignored = session.getIgnoredCount();
			this.error = session.getErrorCount();
			this.current_uri = session.getCurrentURI();
		}
	}
}
