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
package com.qwazr.crawler.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.utils.TimeTracker;

import java.util.concurrent.Future;

final public class FileCrawlStatus extends CrawlStatus<FileCrawlDefinition> {

	@JsonCreator
	FileCrawlStatus(@JsonProperty("node_address") String nodeAddress, @JsonProperty("aborting") Boolean aborting,
			@JsonProperty("aborting_reason") String abortingReason, @JsonProperty("timer") TimeTracker.Status timer,
			@JsonProperty("crawled") Integer crawled, @JsonProperty("ignored") Integer ignored,
			@JsonProperty("redirect") Integer redirect, @JsonProperty("error") Integer error,
			@JsonProperty("last_error") String lastError, @JsonProperty("current_crawl") String currentCrawl,
			@JsonProperty("start_time") final Long startTime, @JsonProperty("end_time") final Long endTime,
			@JsonProperty("current_depth") Integer currentDepth,
			@JsonProperty("crawl_definition") FileCrawlDefinition crawlDefinition,
			@JsonProperty("thread_cancelled") Boolean threadCancelled,
			@JsonProperty("thread_done") Boolean threadDone) {
		super(nodeAddress, aborting, abortingReason, timer, crawled, ignored, redirect, error, lastError, currentCrawl,
				startTime, endTime, currentDepth, crawlDefinition, threadCancelled, threadDone);
	}

	private FileCrawlStatus(Builder builder, boolean withCrawlDefinition) {
		super(builder, withCrawlDefinition);
	}

	public static Builder of(String nodeAddress, TimeTracker timeTracker, FileCrawlDefinition crawlDefinition) {
		return new Builder(nodeAddress, timeTracker, crawlDefinition);
	}

	final public static class Builder extends AbstractBuilder<FileCrawlDefinition, FileCrawlStatus, Builder> {

		private Builder(String nodeAddress, TimeTracker timeTracker, FileCrawlDefinition crawlDefinition) {
			super(Builder.class, nodeAddress, timeTracker, crawlDefinition);
		}

		@Override
		public FileCrawlStatus build(boolean withCrawlDefinition) {
			return new FileCrawlStatus(this, withCrawlDefinition);
		}
	}

}
