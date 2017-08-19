/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.utils.TimeTracker;

public class WebCrawlStatus extends CrawlStatus<WebCrawlDefinition> {

	@JsonCreator
	WebCrawlStatus(@JsonProperty("node_address") String nodeAddress, @JsonProperty("aborting") Boolean aborting,
			@JsonProperty("aborting_reason") String abortingReason, @JsonProperty("timer") TimeTracker.Status timer,
			@JsonProperty("crawled") Integer crawled, @JsonProperty("ignored") Integer ignored,
			@JsonProperty("error") Integer error, @JsonProperty("last_error") String lastError,
			@JsonProperty("current_crawl") String currentCrawl, @JsonProperty("start_time") final Long startTime,
			@JsonProperty("end_time") final Long endTime, @JsonProperty("current_depth") Integer currentDepth,
			@JsonProperty("crawl_definition") WebCrawlDefinition crawlDefinition) {
		super(nodeAddress, aborting, abortingReason, timer, crawled, ignored, error, lastError, currentCrawl, startTime,
				endTime, currentDepth, crawlDefinition);
	}

	private WebCrawlStatus(Builder builder) {
		super(builder);
	}

	public static Builder of(String nodeAddress, TimeTracker timeTracker, WebCrawlDefinition crawlDefinition) {
		return new Builder(nodeAddress, timeTracker, crawlDefinition);
	}

	public static class Builder extends AbstractBuilder<WebCrawlDefinition, WebCrawlStatus> {

		private Builder(String nodeAddress, TimeTracker timeTracker, WebCrawlDefinition crawlDefinition) {
			super(nodeAddress, timeTracker, crawlDefinition);
		}

		@Override
		public WebCrawlStatus build() {
			return new WebCrawlStatus(this);
		}
	}

}
