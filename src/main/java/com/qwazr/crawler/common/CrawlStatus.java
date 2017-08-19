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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.TimeTracker;

import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public abstract class CrawlStatus<T extends CrawlDefinition> {

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

	@JsonProperty("start_time")
	final public Long startTime;

	@JsonProperty("end_time")
	final public Long endTime;

	final public TimeTracker.Status timer;

	/**
	 * the attributes of the session
	 */
	@JsonProperty("crawl_definition")
	final public T crawlDefinition;

	@JsonCreator
	protected CrawlStatus(@JsonProperty("node_address") String nodeAddress, @JsonProperty("aborting") Boolean aborting,
			@JsonProperty("aborting_reason") String abortingReason, @JsonProperty("timer") TimeTracker.Status timer,
			@JsonProperty("crawled") Integer crawled, @JsonProperty("ignored") Integer ignored,
			@JsonProperty("error") Integer error, @JsonProperty("last_error") String lastError,
			@JsonProperty("current_crawl") String currentCrawl, @JsonProperty("start_time") final Long startTime,
			@JsonProperty("end_time") final Long endTime, @JsonProperty("current_depth") Integer currentDepth,
			@JsonProperty("crawl_definition") T crawlDefinition) {
		this.nodeAddress = nodeAddress;
		this.timer = timer;
		this.aborting = aborting;
		this.abortingReason = abortingReason;
		this.crawled = crawled == null ? 0 : crawled;
		this.ignored = ignored == null ? 0 : ignored;
		this.error = error == null ? 0 : error;
		this.lastError = lastError;
		this.currentCrawl = currentCrawl;
		this.currentDepth = currentDepth;
		this.startTime = startTime;
		this.endTime = endTime;
		this.crawlDefinition = crawlDefinition;
	}

	public CrawlStatus(AbstractBuilder<T, ?> builder) {
		this.nodeAddress = builder.nodeAddress;
		this.timer = builder.timeTracker == null ? null : builder.timeTracker.getStatus();
		this.aborting = builder.aborting;
		this.abortingReason = builder.abortingReason;
		this.crawled = builder.crawled;
		this.ignored = builder.ignored;
		this.error = builder.error;
		this.lastError = builder.lastError;
		this.currentCrawl = builder.currentCrawl;
		this.currentDepth = builder.currentDepth;
		this.startTime = builder.startTime;
		this.endTime = builder.endTime;
		this.crawlDefinition = builder.crawlDefinition;
	}

	/**
	 * @return The identifier of the current node
	 */
	@JsonIgnore
	public String getNodeAddress() {
		return nodeAddress;
	}

	/**
	 * @return Is the session currently aborting ?
	 */
	public Boolean getAborting() {
		return aborting;
	}

	/**
	 * @return the aborting reason if any
	 */
	@JsonIgnore
	public String getAbortingReason() {
		return abortingReason;
	}

	/**
	 * @return The number of crawled items
	 */
	public int getCrawled() {
		return crawled;
	}

	/**
	 * @return The number of ignored crawl items
	 */
	public int getIgnored() {
		return ignored;
	}

	/**
	 * @return The number of erroneous crawls
	 */
	public int getError() {
		return error;
	}

	@JsonIgnore
	public String getLastError() {
		return lastError;
	}

	/**
	 * @return the current crawl item
	 */
	@JsonIgnore
	public String getCurrentCrawl() {
		return currentCrawl;
	}

	/**
	 * @return the depth of the current crawled item
	 */
	@JsonIgnore
	public Integer getCurrentDepth() {
		return currentDepth;
	}

	@JsonIgnore
	public Long getStartTime() {
		return startTime;
	}

	@JsonIgnore
	public Long getEndTime() {
		return endTime;
	}

	public TimeTracker.Status getTimer() {
		return timer;
	}

	@JsonIgnore
	public T getCrawlDefinition() {
		return crawlDefinition;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof CrawlStatus))
			return false;
		if (o == this)
			return true;
		final CrawlStatus s = (CrawlStatus) o;
		return Objects.equals(nodeAddress, s.nodeAddress) && Objects.equals(startTime, s.startTime) &&
				Objects.equals(aborting, s.aborting) && Objects.equals(abortingReason, s.abortingReason) &&
				crawled == s.crawled && ignored == s.ignored && error == s.error &&
				Objects.equals(lastError, s.lastError) && Objects.equals(currentCrawl, s.currentCrawl) &&
				Objects.equals(currentDepth, s.currentDepth) && Objects.equals(endTime, s.endTime) &&
				Objects.equals(timer, s.timer) && Objects.equals(crawlDefinition, s.crawlDefinition);
	}

	public abstract static class AbstractBuilder<D extends CrawlDefinition, S extends CrawlStatus<D>> {

		private final String nodeAddress;
		private final long startTime;
		private final TimeTracker timeTracker;
		private final D crawlDefinition;

		private Boolean aborting;
		private String abortingReason;
		private volatile int crawled;
		private volatile int ignored;
		private volatile int error;
		private String lastError;
		private String currentCrawl;
		private Integer currentDepth;
		private Long endTime;

		protected AbstractBuilder(String nodeAddress, TimeTracker timeTracker, D crawlDefinition) {
			this.nodeAddress = nodeAddress;
			this.startTime = System.currentTimeMillis();
			this.timeTracker = timeTracker;
			this.crawlDefinition = crawlDefinition;
		}

		public AbstractBuilder<D, S> abort(String abortingReason) {
			this.aborting = true;
			this.abortingReason = abortingReason;
			return this;
		}

		public AbstractBuilder<D, S> incCrawled() {
			crawled++;
			return this;
		}

		public AbstractBuilder<D, S> incIgnored() {
			ignored++;
			return this;
		}

		public AbstractBuilder<D, S> incError() {
			error++;
			return this;
		}

		public AbstractBuilder<D, S> lastError(String errorMessage) {
			this.lastError = errorMessage;
			return this;
		}

		public AbstractBuilder<D, S> crawl(String currentCrawl, Integer currentDepth) {
			this.currentCrawl = currentCrawl;
			this.currentDepth = currentDepth;
			return this;
		}

		public AbstractBuilder<D, S> done() {
			this.endTime = System.currentTimeMillis();
			return this;
		}

		public abstract S build();
	}

}
