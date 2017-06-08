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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.TimeTracker;

@JsonInclude(Include.NON_NULL)
public class CrawlStatus {

	final public String entry_url;
	final public String node_address;
	final public Boolean aborting;
	final public String aborting_reason;
	final public TimeTracker.Status timer;
	final public UrlStatus urls;

	@JsonCreator
	public CrawlStatus(@JsonProperty("node_address") String nodeAddress, @JsonProperty("aborting") Boolean aborting,
			@JsonProperty("aborting_reason") String abortingReason, @JsonProperty("entry_url") String entryUrl,
			@JsonProperty("timer") TimeTracker.Status timer, @JsonProperty("urls") UrlStatus urlStatus) {
		this.node_address = nodeAddress;
		this.timer = timer;
		this.aborting = aborting;
		this.aborting_reason = abortingReason;
		this.entry_url = entryUrl;
		this.urls = urlStatus;
	}

	public CrawlStatus(final String nodeAddress, final String entryUrl, final CrawlSession session) {
		this(nodeAddress, session.isAborting(), session.getAbortingReason(), entryUrl,
				session.getTimeTracker() == null ? null : session.getTimeTracker().getStatus(), new UrlStatus(session));

	}

	@JsonInclude(Include.NON_EMPTY)
	public static class UrlStatus {

		final public int crawled;
		final public int ignored;
		final public int error;
		final public String current_uri;
		final public Integer current_depth;

		@JsonCreator
		UrlStatus(@JsonProperty("crawled") Integer crawled, @JsonProperty("ignored") Integer ignored,
				@JsonProperty("error") Integer error, @JsonProperty("current_uri") String currentUri,
				@JsonProperty("current_depth") Integer currentDepth) {
			this.crawled = crawled == null ? 0 : crawled;
			this.ignored = ignored == null ? 0 : ignored;
			this.error = error == null ? 0 : error;
			this.current_uri = currentUri;
			this.current_depth = currentDepth;
		}

		private UrlStatus(CrawlSession session) {
			this(session.getCrawledCount(), session.getIgnoredCount(), session.getErrorCount(), session.getCurrentURI(),
					session.getCurrentDepth());
		}
	}

}
