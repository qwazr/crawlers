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
 */
package com.qwazr.crawler.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.common.CrawlDefinition;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.ObjectMappers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
public class WebCrawlDefinition extends CrawlDefinition {

	/**
	 * URL called before a crawl session starts
	 */
	@JsonProperty("pre_url")
	final public String preUrl;

	/**
	 * The entry point URL of the crawl.
	 */
	@JsonProperty("entry_url")
	final public String entryUrl;

	/**
	 * The entry point HTTP Method.
	 */
	@JsonProperty("entry_request")
	final public WebRequestDefinition entryRequest;

	/**
	 * A map of URL to crawl with the depth
	 */
	final public Map<String, Integer> urls;

	/**
	 * The maximum depth of the crawl.
	 */
	@JsonProperty("max_depth")
	final public Integer maxDepth;

	/**
	 * The maximum number of URLs
	 */
	@JsonProperty("max_url_number")
	final public Integer maxUrlNumber;

	/**
	 * A list of regular expression patterns. Any parameters which matches is
	 * removed.
	 */
	@JsonProperty("parameters_patterns")
	final public List<String> parametersPatterns;

	/**
	 * A list of regular expression patterns. Any matching group is remove from the path.
	 */
	@JsonProperty("path_cleaner_patterns")
	final public List<String> pathCleanerPatterns;

	/**
	 * Remove fragments from detected links
	 */
	@JsonProperty("remove_fragments")
	final public Boolean removeFragments;

	/**
	 * Cookies
	 */
	final public Map<String, String> cookies;

	/**
	 * The proxy definition(s)
	 */
	final public List<ProxyDefinition> proxies;

	/**
	 * Support of robots.txt protocol
	 */
	@JsonProperty("robots_txt_enabled")
	final public Boolean robotsTxtEnabled;

	@JsonProperty("user_agent")
	final public String userAgent;

	/**
	 * Time to wait after a crawl
	 */
	@JsonProperty("crawl_wait_ms")
	final public Integer crawlWaitMs;

	/**
	 *
	 */
	@JsonProperty("time_out_sec")
	final public Integer timeOutSecs;

	@JsonCreator
	protected WebCrawlDefinition() {
		preUrl = null;
		entryUrl = null;
		entryRequest = null;
		urls = null;
		maxDepth = null;
		maxUrlNumber = null;
		parametersPatterns = null;
		pathCleanerPatterns = null;
		removeFragments = null;
		robotsTxtEnabled = null;
		userAgent = null;
		cookies = null;
		proxies = null;
		crawlWaitMs = null;
		timeOutSecs = null;
	}

	protected WebCrawlDefinition(Builder builder) {
		super(builder);
		preUrl = builder.preUrl;
		entryUrl = builder.entryUrl;
		entryRequest = builder.entryRequest;
		urls = builder.urls == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(builder.urls));
		maxDepth = builder.maxDepth;
		maxUrlNumber = builder.maxUrlNumber;
		parametersPatterns = builder.parametersPatterns == null ?
				null :
				Collections.unmodifiableList(new ArrayList<>(builder.parametersPatterns));
		pathCleanerPatterns = builder.pathCleanerPatterns == null ?
				null :
				Collections.unmodifiableList(new ArrayList<>(builder.pathCleanerPatterns));
		removeFragments = builder.removeFragments;
		robotsTxtEnabled = builder.robotsTxtEnabled;
		userAgent = builder.userAgent;
		cookies = builder.cookies == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(builder.cookies));
		proxies = builder.proxies == null ? null : Collections.unmodifiableList(new ArrayList<>(builder.proxies));
		crawlWaitMs = builder.crawlWaitMs;
		timeOutSecs = builder.timeOutSecs;
	}

	@JsonIgnore
	public String getPreUrl() {
		return this.preUrl;
	}

	@JsonIgnore
	public String getEntryUrl() {
		return this.entryUrl;
	}

	@JsonIgnore
	public WebRequestDefinition getEntryRequest() {
		return entryRequest;
	}

	public Map<String, Integer> getUrls() {
		return this.urls;
	}

	@JsonIgnore
	public Integer getMaxDepth() {
		return maxDepth;
	}

	@JsonIgnore
	public Integer getMaxUrlNumber() {
		return maxUrlNumber;
	}

	@JsonIgnore
	public Collection<String> getParametersPattern() {
		return parametersPatterns;
	}

	@JsonIgnore
	public Collection<String> getPathCleanerPatterns() {
		return pathCleanerPatterns;
	}

	@JsonIgnore
	public Boolean getRemoveFragments() {
		return removeFragments;
	}

	@JsonIgnore
	public Boolean getRobotsTxtEnabled() {
		return robotsTxtEnabled;
	}

	@JsonIgnore
	public String getUserAgent() {
		return userAgent;
	}

	public Collection<ProxyDefinition> getProxies() {
		return proxies;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	@JsonIgnore
	public Integer getCrawlWaitMs() {
		return crawlWaitMs;
	}

	@JsonIgnore
	public Integer getTimeOutSecs() {
		return timeOutSecs;
	}

	@Override
	public boolean equals(Object o) {
		if (!super.equals(o))
			return false;
		if (!(o instanceof WebCrawlDefinition))
			return false;
		if (o == this)
			return true;
		final WebCrawlDefinition w = (WebCrawlDefinition) o;
		if (!Objects.equals(entryUrl, w.entryUrl))
			return false;
		if (!Objects.equals(entryRequest, w.entryRequest))
			return false;
		if (!CollectionsUtils.equals(urls, w.urls))
			return false;
		if (!Objects.equals(maxDepth, w.maxDepth))
			return false;
		if (!Objects.equals(maxUrlNumber, w.maxUrlNumber))
			return false;
		if (!CollectionsUtils.equals(parametersPatterns, w.parametersPatterns))
			return false;
		if (!CollectionsUtils.equals(pathCleanerPatterns, w.pathCleanerPatterns))
			return false;
		if (!Objects.equals(removeFragments, w.removeFragments))
			return false;
		if (!Objects.equals(robotsTxtEnabled, w.robotsTxtEnabled))
			return false;
		if (!Objects.equals(userAgent, w.userAgent))
			return false;
		if (!CollectionsUtils.equals(cookies, w.cookies))
			return false;
		if (!CollectionsUtils.equals(proxies, w.proxies))
			return false;
		if (!Objects.equals(crawlWaitMs, w.crawlWaitMs))
			return false;
		if (!Objects.equals(timeOutSecs, w.timeOutSecs))
			return false;
		return true;
	}

	public static WebCrawlDefinition newInstance(final String json) throws IOException {
		return ObjectMappers.JSON.readValue(json, WebCrawlDefinition.class);
	}

	public static Builder of() {
		return new Builder();
	}

	public static Builder of(WebCrawlDefinition webCrawlDefinition) {
		return new Builder(webCrawlDefinition);
	}

	public static class Builder extends AbstractBuilder<WebCrawlDefinition, Builder> {

		private String preUrl;
		private String entryUrl;
		private WebRequestDefinition entryRequest;
		private LinkedHashMap<String, Integer> urls;
		private Integer maxDepth;
		private Integer maxUrlNumber;
		private List<String> parametersPatterns;
		private List<String> pathCleanerPatterns;
		private Boolean removeFragments;
		private LinkedHashMap<String, String> cookies;
		private List<ProxyDefinition> proxies;
		private Boolean robotsTxtEnabled;
		private String userAgent;
		private Integer crawlWaitMs;
		private Integer timeOutSecs;

		protected Builder() {
			super(Builder.class);
		}

		protected Builder(WebCrawlDefinition src) {
			super(Builder.class, src);
			this.preUrl = src.preUrl;
			this.entryUrl = src.entryUrl;
			this.entryRequest = src.entryRequest;
			this.urls = src.urls == null ? null : new LinkedHashMap<>(src.urls);
			this.maxDepth = src.maxDepth;
			this.maxUrlNumber = src.maxUrlNumber;
			this.parametersPatterns = src.parametersPatterns == null ? null : new ArrayList<>(src.parametersPatterns);
			this.pathCleanerPatterns =
					src.pathCleanerPatterns == null ? null : new ArrayList<>(src.pathCleanerPatterns);
			this.removeFragments = src.removeFragments;
			this.cookies = src.cookies == null ? null : new LinkedHashMap<>(src.cookies);
			this.proxies = src.proxies == null ? null : new ArrayList<>(src.proxies);
			this.robotsTxtEnabled = src.robotsTxtEnabled;
			this.userAgent = src.userAgent;
			this.crawlWaitMs = src.crawlWaitMs;
			this.timeOutSecs = src.timeOutSecs;
		}

		public Builder setUrls(final LinkedHashMap<String, Integer> urls) {
			this.urls = urls;
			return this;
		}

		public Builder setPreUrl(final String preUrl) {
			this.preUrl = preUrl;
			return this;
		}

		public Builder setEntryUrl(final String entryUrl) {
			this.entryUrl = entryUrl;
			return this;
		}

		public Builder setEntryRequest(final WebRequestDefinition request) {
			this.entryRequest = request;
			return this;
		}

		public Builder addUrl(final String url, final Integer depth) {
			if (urls == null)
				urls = new LinkedHashMap<>();
			urls.put(url, depth);
			return this;
		}

		public Builder setMaxDepth(final Integer maxDepth) {
			this.maxDepth = maxDepth;
			return this;
		}

		public Builder setMaxUrlNumber(final Integer maxUrlNumber) {
			this.maxUrlNumber = maxUrlNumber;
			return this;
		}

		public Builder addParametersPattern(final String parametersPattern) {
			if (this.parametersPatterns == null)
				this.parametersPatterns = new ArrayList<>();
			this.parametersPatterns.add(parametersPattern);
			return this;
		}

		public Builder addPathCleanerPattern(final String pathCleanerPattern) {
			if (this.pathCleanerPatterns == null)
				this.pathCleanerPatterns = new ArrayList<>();
			this.pathCleanerPatterns.add(pathCleanerPattern);
			return this;
		}

		public Builder setRemoveFragments(final Boolean removeFragments) {
			this.removeFragments = removeFragments;
			return this;
		}

		public Builder setRobotsTxtEnabled(final Boolean robotsTxtEnabled) {
			this.robotsTxtEnabled = robotsTxtEnabled;
			return this;
		}

		public Builder addProxy(final ProxyDefinition proxy) {
			if (proxy == null)
				return this;
			if (proxies == null)
				proxies = new ArrayList<>();
			proxies.add(proxy);
			return this;
		}

		public Builder addCookie(final String name, final String value) {
			if (name == null || value == null)
				return this;
			if (cookies == null)
				cookies = new LinkedHashMap<>();
			cookies.put(name, value);
			return this;
		}

		public Builder setCookies(final Map<String, String> cookies) {
			if (this.cookies == null)
				this.cookies = new LinkedHashMap<>();
			if (cookies != null)
				this.cookies.putAll(cookies);
			return this;
		}

		public Builder setCrawlWaitMs(Integer crawlWaitMs) {
			this.crawlWaitMs = crawlWaitMs;
			return this;
		}

		public Builder setTimeOutSecs(Integer timeOutSecs) {
			this.timeOutSecs = timeOutSecs;
			return this;
		}

		@Override
		public WebCrawlDefinition build() {
			return new WebCrawlDefinition(this);
		}
	}
}
