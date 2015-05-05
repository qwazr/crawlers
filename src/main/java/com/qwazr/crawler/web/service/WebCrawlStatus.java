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
package com.qwazr.crawler.web.service;

import java.lang.Thread.State;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.qwazr.crawler.web.manager.CurrentSession;

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
			this.current_uri = session.getCurrentUri();
		}
	}
}
