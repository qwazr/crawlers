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
import com.qwazr.crawler.web.driver.BrowserDriverEnum;
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
	 * The initial browser type.
	 */
	@JsonProperty("browser_type")
	final public BrowserDriverEnum browserType;

	/**
	 * The language used by the browser
	 */
	@JsonProperty("browser_language")
	final public String browserLanguage;

	/**
	 * The version of the browser
	 */
	@JsonProperty("browser_version")
	final public String browserVersion;

	/**
	 * The name of the browser
	 */
	@JsonProperty("browser_name")
	final public String browserName;

	/**
	 * Enable or disable javascript
	 */
	@JsonProperty("javascript_enabled")
	final public Boolean javascriptEnabled;

	/**
	 * Download images
	 */
	@JsonProperty("download_images")
	final public Boolean downloadImages;

	/**
	 * Enable Web security (default is true)
	 */
	@JsonProperty("web_security")
	final public Boolean webSecurity;

	/**
	 * Cookies
	 */
	final public Map<String, String> cookies;

	/**
	 * The proxy definition(s)
	 */
	final public ProxyDefinition proxy;

	final public List<ProxyDefinition> proxies;

	/**
	 * Support of robots.txt protocol
	 */
	@JsonProperty("robots_txt_enabled")
	final public Boolean robotsTxtEnabled;

	@JsonProperty("robots_txt_useragent")
	final public String robotsTxtUseragent;

	/**
	 * Time wait on successfull crawl
	 */
	@JsonProperty("crawl_wait_ms")
	final public Integer crawlWaitMs;

	/**
	 *
	 */
	@JsonProperty("implicitly_wait")
	final public Integer implicitlyWait;

	@JsonProperty("script_timeout")
	final public Integer scriptTimeout;

	@JsonProperty("page_load_timeout")
	final public Integer pageLoadTimeout;

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
		browserName = null;
		browserLanguage = null;
		browserVersion = null;
		browserType = null;
		javascriptEnabled = null;
		downloadImages = null;
		webSecurity = null;
		robotsTxtEnabled = null;
		robotsTxtUseragent = null;
		cookies = null;
		proxy = null;
		proxies = null;
		implicitlyWait = null;
		scriptTimeout = null;
		pageLoadTimeout = null;
		crawlWaitMs = null;
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
		browserName = builder.browserName;
		browserLanguage = builder.browserLanguage;
		browserVersion = builder.browserVersion;
		browserType = builder.browserType;
		javascriptEnabled = builder.javascriptEnabled;
		downloadImages = builder.downloadImages;
		webSecurity = builder.webSecurity;
		robotsTxtEnabled = builder.robotsTxtEnabled;
		robotsTxtUseragent = builder.robotsTxtUseragent;
		cookies = builder.cookies == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(builder.cookies));
		proxy = builder.proxy;
		proxies = builder.proxies == null ? null : Collections.unmodifiableList(new ArrayList<>(builder.proxies));
		implicitlyWait = builder.implicitlyWait;
		scriptTimeout = builder.scriptTimeout;
		pageLoadTimeout = builder.pageLoadTimeout;
		crawlWaitMs = builder.crawlWaitMs;
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
	public Boolean getJavascriptEnabled() {
		return javascriptEnabled;
	}

	@JsonIgnore
	public Boolean getDownloadImages() {
		return downloadImages;
	}

	@JsonIgnore
	public Boolean getWebSecurity() {
		return webSecurity;
	}

	@JsonIgnore
	public Boolean getRobotsTxtEnabled() {
		return robotsTxtEnabled;
	}

	@JsonIgnore
	public String getRobotsTxtUserAgent() {
		return robotsTxtUseragent;
	}

	@JsonIgnore
	public BrowserDriverEnum getBrowserType() {
		return browserType;
	}

	@JsonIgnore
	public String getBrowserName() {
		return browserName;
	}

	@JsonIgnore
	public String getBrowserVersion() {
		return browserVersion;
	}

	@JsonIgnore
	public String getBrowserLanguage() {
		return browserLanguage;
	}

	public Collection<ProxyDefinition> getProxies() {
		return proxies;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	@JsonIgnore
	public Integer getImplicitlyWait() {
		return implicitlyWait;
	}

	@JsonIgnore
	public Integer getScriptTimeout() {
		return scriptTimeout;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	@JsonIgnore
	public Integer getCrawlWaitMs() {
		return crawlWaitMs;
	}

	@JsonIgnore
	public Integer getPageLoadTimeout() {
		return pageLoadTimeout;
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
		if (!Objects.equals(browserName, w.browserName))
			return false;
		if (!Objects.equals(browserLanguage, w.browserLanguage))
			return false;
		if (!Objects.equals(browserVersion, w.browserVersion))
			return false;
		if (!Objects.equals(browserType, w.browserType))
			return false;
		if (!Objects.equals(javascriptEnabled, w.javascriptEnabled))
			return false;
		if (!Objects.equals(downloadImages, w.downloadImages))
			return false;
		if (!Objects.equals(webSecurity, w.webSecurity))
			return false;
		if (!Objects.equals(robotsTxtEnabled, w.robotsTxtEnabled))
			return false;
		if (!Objects.equals(robotsTxtUseragent, w.robotsTxtUseragent))
			return false;
		if (!CollectionsUtils.equals(cookies, w.cookies))
			return false;
		if (!Objects.equals(proxy, w.proxy))
			return false;
		if (!CollectionsUtils.equals(proxies, w.proxies))
			return false;
		if (!Objects.equals(implicitlyWait, w.implicitlyWait))
			return false;
		if (!Objects.equals(scriptTimeout, w.scriptTimeout))
			return false;
		if (!Objects.equals(pageLoadTimeout, w.pageLoadTimeout))
			return false;
		if (!Objects.equals(crawlWaitMs, w.crawlWaitMs))
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
		private BrowserDriverEnum browserType;
		private String browserLanguage;
		private String browserVersion;
		private String browserName;
		private Boolean javascriptEnabled;
		private Boolean downloadImages;
		private Boolean webSecurity;
		private LinkedHashMap<String, String> cookies;
		private ProxyDefinition proxy;
		private List<ProxyDefinition> proxies;
		private Boolean robotsTxtEnabled;
		private String robotsTxtUseragent;
		private Integer crawlWaitMs;
		private Integer implicitlyWait;
		private Integer scriptTimeout;
		private Integer pageLoadTimeout;

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
			this.browserType = src.browserType;
			this.browserLanguage = src.browserLanguage;
			this.browserVersion = src.browserVersion;
			this.browserName = src.browserName;
			this.javascriptEnabled = src.javascriptEnabled;
			this.downloadImages = src.downloadImages;
			this.webSecurity = src.webSecurity;
			this.cookies = src.cookies == null ? null : new LinkedHashMap<>(src.cookies);
			this.proxy = src.proxy;
			this.proxies = src.proxies == null ? null : new ArrayList<>(src.proxies);
			this.robotsTxtEnabled = src.robotsTxtEnabled;
			this.robotsTxtUseragent = src.robotsTxtUseragent;
			this.crawlWaitMs = src.crawlWaitMs;
			this.implicitlyWait = src.implicitlyWait;
			this.scriptTimeout = src.scriptTimeout;
			this.pageLoadTimeout = src.pageLoadTimeout;
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

		public Builder setJavascriptEnabled(final Boolean javascriptEnabled) {
			this.javascriptEnabled = javascriptEnabled;
			return this;
		}

		public Builder setDownloadImages(final Boolean downloadImages) {
			this.downloadImages = downloadImages;
			return this;
		}

		public Builder setWebSecurity(final Boolean webSecurity) {
			this.webSecurity = webSecurity;
			return this;
		}

		public Builder setRobotsTxtEnabled(final Boolean robotsTxtEnabled) {
			this.robotsTxtEnabled = robotsTxtEnabled;
			return this;
		}

		public Builder setRobotsTxtUserAgent(final String robotsTxtUserAgent) {
			this.robotsTxtUseragent = robotsTxtUserAgent;
			return this;
		}

		public Builder setBrowserType(final String browserType) {
			this.browserType = BrowserDriverEnum.valueOf(browserType);
			return this;
		}

		public Builder setBrowserName(final String browserName) {
			this.browserName = browserName;
			return this;
		}

		public Builder setBrowserVersion(final String browserVersion) {
			this.browserVersion = browserVersion;
			return this;
		}

		public Builder setBrowserLanguage(final String browserLanguage) {
			this.browserLanguage = browserLanguage;
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

		public Builder setImplicitlyWait(final Integer wait) {
			this.implicitlyWait = wait;
			return this;
		}

		public Builder setScriptTimeout(final Integer timeout) {
			this.scriptTimeout = timeout;
			return this;
		}

		public Builder setCrawlWaitMs(Integer crawlWaitMs) {
			this.crawlWaitMs = crawlWaitMs;
			return this;
		}

		public Builder setPageLoadTimeout(Integer timeout) {
			this.pageLoadTimeout = timeout;
			return this;
		}

		@Override
		public WebCrawlDefinition build() {
			return new WebCrawlDefinition(this);
		}
	}
}
