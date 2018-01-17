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
 */
package com.qwazr.crawler.ftp;

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
		fieldVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
public class FtpCrawlDefinition extends CrawlDefinition {

	/**
	 * The host name of the FTP server.
	 */
	@JsonProperty("hostname")
	final public String hostname;

	/**
	 * The entry point PATH of the crawl.
	 */
	@JsonProperty("entry_path")
	final public String entryPath;

	/**
	 * The username to use for the FTP connection
	 */
	@JsonProperty("username")
	final public String username;

	/**
	 * The password to use for the FTP connection
	 */
	@JsonProperty("password")
	final public String password;

	/**
	 * Enable FTP over SSL
	 */
	@JsonProperty("is_ssl")
	final public Boolean isSsl;

	/**
	 * Set the data connection mode to passive
	 */
	@JsonProperty("is_passive")
	final public Boolean isPassive;

	@JsonCreator
	protected FtpCrawlDefinition(@JsonProperty("max_depth") Integer maxDepth,
			@JsonProperty("inclusion_patterns") Collection<String> inclusionPatterns,
			@JsonProperty("exclusion_patterns") Collection<String> exclusionPatterns,
			@JsonProperty("crawl_wait_ms") Integer crawlWaitMs,
			@JsonProperty("variables") LinkedHashMap<String, String> variables,
			@JsonProperty("scripts") Map<EventEnum, ScriptDefinition> scripts,
			@JsonProperty("hostname") String hostname, @JsonProperty("entry_path") String entryPath,
			@JsonProperty("username") String username, @JsonProperty("password") String password,
			@JsonProperty("is_ssl") Boolean isSsl, @JsonProperty("is_passive") Boolean isPassive) {
		super(variables, scripts, inclusionPatterns, exclusionPatterns, maxDepth, crawlWaitMs);
		this.hostname = hostname;
		this.entryPath = entryPath;
		this.username = username;
		this.password = password;
		this.isSsl = isSsl;
		this.isPassive = isPassive;
	}

	private FtpCrawlDefinition(Builder builder) {
		super(builder);
		this.hostname = builder.hostname;
		this.entryPath = builder.entryPath;
		this.username = builder.username;
		this.password = builder.password;
		this.isSsl = builder.isSsl;
		this.isPassive = builder.isPassive;
	}

	@Override
	public boolean equals(final Object o) {
		if (!super.equals(o))
			return false;
		if (!(o instanceof FtpCrawlDefinition))
			return false;
		if (o == this)
			return true;
		final FtpCrawlDefinition f = (FtpCrawlDefinition) o;
		return Objects.equals(hostname, f.hostname) && Objects.equals(entryPath, f.entryPath) &&
				Objects.equals(username, f.username) && Objects.equals(password, f.password) &&
				Objects.equals(isSsl, f.isSsl) && Objects.equals(isPassive, f.isPassive);
	}

	@JsonIgnore
	public String getHostName() {
		return this.hostname;
	}

	@JsonIgnore
	public String getEntryPath() {
		return this.entryPath;
	}

	@JsonIgnore
	public String getUsername() {
		return username;
	}

	@JsonIgnore
	public String getPassword() {
		return password;
	}

	@JsonIgnore
	public Boolean isSsl() {
		return isSsl;
	}

	@JsonIgnore
	public Boolean isPassive() {
		return isPassive;
	}

	public static Builder of() {
		return new Builder();
	}

	public static Builder of(FtpCrawlDefinition crawlDefinition) {
		return new Builder(crawlDefinition);
	}

	public static class Builder extends AbstractBuilder<FtpCrawlDefinition, Builder> {

		private String hostname;

		private String entryPath;

		private String username;

		private String password;

		private Boolean isSsl;

		private Boolean isPassive;

		protected Builder() {
			super(Builder.class);
		}

		protected Builder(FtpCrawlDefinition crawlDefinition) {
			super(Builder.class, crawlDefinition);
			entryPath = crawlDefinition.entryPath;
			maxDepth = crawlDefinition.maxDepth;
			crawlWaitMs = crawlDefinition.crawlWaitMs;
		}

		public Builder hostname(final String hostname) {
			this.hostname = hostname;
			return this;
		}

		public Builder username(final String username) {
			this.username = username;
			return this;
		}

		public Builder password(final String password) {
			this.password = password;
			return this;
		}

		public Builder ssl(final Boolean ssl) {
			this.isSsl = ssl;
			return this;
		}

		public Builder passive(final Boolean passive) {
			this.isPassive = passive;
			return this;
		}

		public Builder entryPath(final String entryPath) {
			this.entryPath = entryPath;
			return this;
		}

		@Override
		public FtpCrawlDefinition build() {
			return new FtpCrawlDefinition(this);
		}
	}

	@JsonIgnore
	public static FtpCrawlDefinition newInstance(final String json) throws IOException {
		return ObjectMappers.JSON.readValue(json, FtpCrawlDefinition.class);
	}

}
