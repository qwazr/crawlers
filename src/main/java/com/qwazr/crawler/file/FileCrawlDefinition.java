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
import com.qwazr.crawler.common.CrawlDefinition;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonInclude(Include.NON_EMPTY)
public class FileCrawlDefinition extends CrawlDefinition {

	/**
	 * The entry point PATH of the crawl.
	 */
	public String entry_path = null;

	/**
	 * A list of regular expression patterns. An URL may not be crawled if it
	 * matches any pattern.
	 */
	public List<String> exclusion_patterns = null;

	/**
	 * Time wait on successfull crawl
	 */
	public Integer crawl_wait_ms = null;

	public FileCrawlDefinition() {
	}

	protected FileCrawlDefinition(FileCrawlDefinition src) {
		super(src);
		entry_path = src.entry_path;
		exclusion_patterns = src.exclusion_patterns == null ? null : new ArrayList<>(src.exclusion_patterns);
		crawl_wait_ms = src.crawl_wait_ms;
	}

	public Object clone() {
		return new FileCrawlDefinition(this);
	}

	@JsonIgnore
	public FileCrawlDefinition setEntryPath(final String entryPath) {
		this.entry_path = entryPath;
		return this;
	}

	@JsonIgnore
	public String getEntryPath() {
		return this.entry_path;
	}

	@JsonIgnore
	public FileCrawlDefinition setExclusionPattern(final String exclusionPatternText) throws IOException {
		if (exclusionPatternText == null) {
			exclusion_patterns = null;
			return this;
		}
		exclusion_patterns = new ArrayList<>();
		StringUtils.linesCollector(exclusionPatternText, false, exclusion_patterns);
		return this;
	}

	@JsonIgnore
	public FileCrawlDefinition addExclusionPattern(final String exclusionPattern) {
		if (exclusion_patterns == null)
			exclusion_patterns = new ArrayList<>();
		exclusion_patterns.add(exclusionPattern);
		return this;
	}

	@JsonIgnore
	public Collection<String> getExclusionPatterns() {
		return exclusion_patterns;
	}

	@JsonIgnore
	public FileCrawlDefinition setCrawlWaitMs(Integer crawlWaitMs) {
		this.crawl_wait_ms = crawlWaitMs;
		return this;
	}

	@JsonIgnore
	public Integer getCrawlWaitMs() {
		return crawl_wait_ms;
	}

	@JsonIgnore
	public static FileCrawlDefinition newInstance(final String json) throws IOException {
		return JsonMapper.MAPPER.readValue(json, FileCrawlDefinition.class);
	}

}
