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

	/**
	 * The entry point of the crawl
	 */
	@JsonProperty("entry_crawl")
	final public String entryCrawl;

	/**
	 * The identifier of the current node
	 */
	@JsonProperty("node_address")
	final public String nodeAddress;

	/**
	 * Is the session currently aborting ?
	 */
	final public Boolean aborting;

	/**
	 * the aborting reason if any
	 */
	@JsonProperty("aborting_reason")
	final public String abortingReason;

	/**
	 * The number of crawled items
	 */
	final public int crawled;

	/**
	 * The number of ignored crawl items
	 */
	final public int ignored;

	/**
	 * The number of erroneous crawls
	 */
	final public int error;

	@JsonProperty("last_error")
	final public String lastError;

	/**
	 * the current crawl item
	 */
	@JsonProperty("current_crawl")
	final public String currentCrawl;

	/**
	 * the depth of the current crawled item
	 */
	@JsonProperty("current_depth")
	final public Integer currentDepth;

	final public TimeTracker.Status timer;

	@JsonCreator
	public CrawlStatus(@JsonProperty("node_address") String nodeAddress, @JsonProperty("aborting") Boolean aborting,
			@JsonProperty("aborting_reason") String abortingReason, @JsonProperty("entry_crawl") String entryCrawl,
			@JsonProperty("timer") TimeTracker.Status timer, @JsonProperty("crawled") Integer crawled,
			@JsonProperty("ignored") Integer ignored, @JsonProperty("error") Integer error,
			@JsonProperty("last_error") String lastError, @JsonProperty("current_crawl") String currentCrawl,
			@JsonProperty("current_depth") Integer currentDepth) {
		this.nodeAddress = nodeAddress;
		this.timer = timer;
		this.aborting = aborting;
		this.abortingReason = abortingReason;
		this.entryCrawl = entryCrawl;
		this.crawled = crawled == null ? 0 : crawled;
		this.ignored = ignored == null ? 0 : ignored;
		this.error = error == null ? 0 : error;
		this.lastError = lastError;
		this.currentCrawl = currentCrawl;
		this.currentDepth = currentDepth;
	}

	public CrawlStatus(Builder builder) {
		this.nodeAddress = builder.nodeAddress;
		this.timer = builder.timeTracker == null ? null : builder.timeTracker.getStatus();
		this.aborting = builder.aborting;
		this.abortingReason = builder.abortingReason;
		this.entryCrawl = builder.entryCrawl;
		this.crawled = builder.crawled;
		this.ignored = builder.ignored;
		this.error = builder.error;
		this.lastError = builder.lastError;
		this.currentCrawl = builder.currentCrawl;
		this.currentDepth = builder.currentDepth;
	}

	public static Builder of(String entryCrawl, String nodeAddress, TimeTracker timeTracker) {
		return new Builder(entryCrawl, nodeAddress, timeTracker);
	}

	public static class Builder {

		private final String entryCrawl;
		private final String nodeAddress;
		private TimeTracker timeTracker;

		private Boolean aborting;
		private String abortingReason;
		private volatile int crawled;
		private volatile int ignored;
		private volatile int error;
		private String lastError;
		private String currentCrawl;
		private Integer currentDepth;

		private Builder(String entryCrawl, String nodeAddress, TimeTracker timeTracker) {
			this.entryCrawl = entryCrawl;
			this.nodeAddress = nodeAddress;
			this.timeTracker = timeTracker;
		}

		public Builder abort(String abortingReason) {
			this.aborting = true;
			this.abortingReason = abortingReason;
			return this;
		}

		public Builder incCrawled() {
			crawled++;
			return this;
		}

		public Builder incIgnored() {
			ignored++;
			return this;
		}

		public Builder error(String errorMessage) {
			error++;
			this.lastError = errorMessage;
			return this;
		}

		public Builder crawl(String currentCrawl, Integer currentDepth) {
			this.currentCrawl = currentCrawl;
			this.currentDepth = currentDepth;
			return this;
		}

		public CrawlStatus build() {
			return new CrawlStatus(this);
		}
	}

}
