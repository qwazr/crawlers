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
 **/
package com.qwazr.crawler.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.common.CrawlDefinition;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.common.ScriptDefinition;
import com.qwazr.utils.ObjectMappers;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
public class FileCrawlDefinition extends CrawlDefinition {

	/**
	 * The entry point PATH of the crawl.
	 */
	@JsonProperty("entry_path")
	final public String entryPath;

	/**
	 * The maximum number of directory levels to visit.
	 */
	@JsonProperty("max_depth")
	final public Integer maxDepth;

	/**
	 * Time wait on successfull crawl
	 */
	@JsonProperty("crawl_wait_ms")
	final public Integer crawlWaitMs;

	protected FileCrawlDefinition(@JsonProperty("entry_path") String entryPath,
			@JsonProperty("max_depth") Integer maxDepth,
			@JsonProperty("inclusion_patterns") Collection<String> inclusionPatterns,
			@JsonProperty("exclusion_patterns") Collection<String> exclusionPatterns,
			@JsonProperty("crawl_wait_ms") Integer crawlWaitMs,
			@JsonProperty("variables") LinkedHashMap<String, String> variables,
			@JsonProperty("scripts") Map<EventEnum, ScriptDefinition> scripts) {
		super(variables, scripts, inclusionPatterns, exclusionPatterns);
		this.entryPath = entryPath;
		this.maxDepth = maxDepth;
		this.crawlWaitMs = crawlWaitMs;
	}

	@Override
	public boolean equals(final Object o) {
		if (!super.equals(o))
			return false;
		if (!(o instanceof FileCrawlDefinition))
			return false;
		if (o == this)
			return true;
		final FileCrawlDefinition f = (FileCrawlDefinition) o;
		if (!Objects.equals(entryPath, f.entryPath))
			return false;
		if (!Objects.equals(maxDepth, f.maxDepth))
			return false;
		if (!Objects.equals(crawlWaitMs, f.crawlWaitMs))
			return false;
		return true;
	}

	@JsonIgnore
	public String getEntryPath() {
		return this.entryPath;
	}

	@JsonIgnore
	public Integer getMaxDepth() {
		return this.maxDepth;
	}

	@JsonIgnore
	public Integer getCrawlWaitMs() {
		return crawlWaitMs;
	}

	public static Builder of() {
		return new Builder();
	}

	public static Builder of(FileCrawlDefinition crawlDefinition) {
		return new Builder(crawlDefinition);
	}

	public static class Builder extends AbstractBuilder<FileCrawlDefinition, Builder> {

		private String entryPath;

		private Integer maxDepth;

		private Integer crawlWaitMs;

		protected Builder() {
		}

		protected Builder(FileCrawlDefinition crawlDefinition) {
			super(crawlDefinition);
			entryPath = crawlDefinition.entryPath;
			maxDepth = crawlDefinition.maxDepth;
			crawlWaitMs = crawlDefinition.crawlWaitMs;
		}

		public Builder entryPath(final String entryPath) {
			this.entryPath = entryPath;
			return this;
		}

		public Builder maxDepth(final Integer maxDepth) {
			this.maxDepth = maxDepth;
			return this;
		}

		@JsonIgnore
		public Builder setCrawlWaitMs(Integer crawlWaitMs) {
			this.crawlWaitMs = crawlWaitMs;
			return this;
		}

		@Override
		public FileCrawlDefinition build() {
			return new FileCrawlDefinition(entryPath, maxDepth, inclusionPatterns, exclusionPatterns, crawlWaitMs,
					variables, scripts);
		}
	}

	@JsonIgnore
	public static FileCrawlDefinition newInstance(final String json) throws IOException {
		return ObjectMappers.JSON.readValue(json, FileCrawlDefinition.class);
	}

}
