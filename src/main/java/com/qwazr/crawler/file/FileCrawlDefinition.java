/*
 * Copyright 2017-2018 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
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
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		creatorVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE,
		fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class FileCrawlDefinition extends CrawlDefinition {

	/**
	 * The entry point PATH of the crawl.
	 */
	@JsonProperty("entry_path")
	final public String entryPath;

	@JsonCreator
	protected FileCrawlDefinition(@JsonProperty("entry_path") String entryPath,
			@JsonProperty("max_depth") Integer maxDepth,
			@JsonProperty("inclusion_patterns") Collection<String> inclusionPatterns,
			@JsonProperty("exclusion_patterns") Collection<String> exclusionPatterns,
			@JsonProperty("crawl_wait_ms") Integer crawlWaitMs,
			@JsonProperty("variables") LinkedHashMap<String, Object> variables,
			@JsonProperty("scripts") Map<EventEnum, ScriptDefinition> scripts) {
		super(variables, scripts, inclusionPatterns, exclusionPatterns, maxDepth, crawlWaitMs);
		this.entryPath = entryPath;
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

		protected Builder() {
			super(Builder.class);
		}

		protected Builder(FileCrawlDefinition crawlDefinition) {
			super(Builder.class, crawlDefinition);
			entryPath = crawlDefinition.entryPath;
			maxDepth = crawlDefinition.maxDepth;
			crawlWaitMs = crawlDefinition.crawlWaitMs;
		}

		public Builder entryPath(final String entryPath) {
			this.entryPath = entryPath;
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
