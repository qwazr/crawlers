/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.web.driver.BrowserDriverEnum;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@JsonInclude(Include.NON_EMPTY)
public class WebCrawlDefinition implements Cloneable {

	/**
	 * The entry point URL of the crawl.
	 */
	public String entry_url = null;

	/**
	 * The entry point HTTP Method.
	 */
	public WebRequestDefinition entry_request = null;

	/**
	 * A map of URL to crawl with the depth
	 */
	public LinkedHashMap<String, Integer> urls = null;

	/**
	 * The maximum depth of the crawl.
	 */
	public Integer max_depth = null;

	/**
	 * The maximum number of URLs
	 */
	public Integer max_url_number = null;

	/**
	 * A list of regular expression patterns. Any parameters which matches is
	 * removed.
	 */
	public List<String> parameters_patterns = null;

	/**
	 * A list of regular expression patterns. An URL may be crawled only if it
	 * matches any pattern.
	 */
	public List<String> inclusion_patterns = null;

	/**
	 * A list of regular expression patterns. An URL may not be crawled if it
	 * matches any pattern.
	 */
	public List<String> exclusion_patterns = null;

	/**
	 * Remove fragments from detected links
	 */
	public Boolean remove_fragments = null;

	/**
	 * The initial browser type.
	 */
	public BrowserDriverEnum browser_type = null;

	/**
	 * The language used by the browser
	 */
	public String browser_language = null;

	/**
	 * The version of the browser
	 */
	public String browser_version = null;

	/**
	 * The name of the browser
	 */
	public String browser_name = null;

	/**
	 * Enable or disable javascript
	 */
	public Boolean javascript_enabled = null;

	/**
	 * Download images
	 */
	public Boolean download_images = null;

	/**
	 * Enable Web security  (default is true)
	 */
	public Boolean web_security = null;

	/**
	 * Cookies
	 */
	public Map<String, String> cookies = null;

	/**
	 * The proxy definition(s)
	 */
	public ProxyDefinition proxy = null;

	public List<ProxyDefinition> proxies = null;

	/**
	 * Support of robots.txt protocol
	 */
	public Boolean robots_txt_enabled = null;
	public String robots_txt_useragent = null;

	@JsonInclude(Include.NON_EMPTY)
	public static class ProxyDefinition implements Cloneable {

		public Boolean enabled = null;

		/**
		 * the proxy host for FTP connections, expected format is
		 * <code>hostname:1234</code>
		 */
		public String ftp_proxy = null;

		/**
		 * the proxy host for HTTP connections, expected format is
		 * <code>hostname:1234</code>
		 */
		public String http_proxy = null;

		/**
		 * The proxy bypass (noproxy) addresses
		 */
		public String no_proxy = null;

		/**
		 * the proxy host for SSL connections, expected format is
		 * <code>hostname:1234</code>
		 */
		public String ssl_proxy = null;

		/**
		 * the proxy host for SOCKS v5 connections, expected format is
		 * <code>hostname:1234</code>
		 */
		public String socks_proxy = null;

		/**
		 * the SOCKS proxy's username
		 */
		public String socks_username = null;

		/**
		 * the SOCKS proxy's password
		 */
		@JsonIgnore
		public String socks_password = null;

		/**
		 * Specifies the URL to be used for proxy auto-configuration. Expected
		 * format is <code>http://hostname.com:1234/pacfile</code>
		 */
		public String proxy_autoconfig_url = null;

		public ProxyDefinition() {
		}

		protected ProxyDefinition(ProxyDefinition src) {
			ftp_proxy = src.ftp_proxy;
			http_proxy = src.http_proxy;
			no_proxy = src.no_proxy;
			ssl_proxy = src.ssl_proxy;
			socks_proxy = src.socks_proxy;
			socks_username = src.socks_username;
			socks_password = src.socks_password;
			proxy_autoconfig_url = src.proxy_autoconfig_url;
		}

		@JsonProperty("socks_password")
		private void setSocks_password(String socks_password) {
			this.socks_password = socks_password;
		}

		@Override
		public Object clone() {
			return new ProxyDefinition(this);
		}

		public ProxyDefinition setHttpProxy(String http_proxy) {
			this.http_proxy = http_proxy;
			return this;
		}

	}

	/**
	 *
	 */
	public Integer implicitly_wait = null;

	public Integer script_timeout = null;

	public Integer page_load_timeout = null;

	/**
	 * The global variables shared by all the scripts.
	 */
	public Map<String, String> variables = null;

	/**
	 * A list of scripts paths mapped with the events which fire the scripts.
	 */
	public Map<EventEnum, Script> scripts = null;

	public enum EventEnum {

		/**
		 * Executed before the crawl session start
		 */
		before_session,

		/**
		 * Executed after the crawl session ends
		 */
		after_session,

		/**
		 * Executed before an URL is crawled
		 */
		before_crawl,

		/**
		 * Executed after an URL has been crawled
		 */
		after_crawl
	}

	@JsonInclude(Include.NON_EMPTY)
	public static class Script implements Cloneable {

		/**
		 * The path to the scripts
		 */
		public String name = null;

		/**
		 * The local variables passed to the scripts
		 */
		public Map<String, String> variables = null;

		public Script() {
		}

		public Script(String name) {
			this.name = name;
		}

		protected Script(Script src) {
			this.name = src.name;
			this.variables = src.variables == null ? null : new HashMap<String, String>(src.variables);
		}

		@Override
		final public Object clone() {
			return new Script(this);
		}

		public Script addVariable(String name, String value) {
			if (variables == null)
				variables = new HashMap<>();
			variables.put(name, value);
			return this;
		}

	}

	public WebCrawlDefinition() {
	}

	protected WebCrawlDefinition(WebCrawlDefinition src) {
		entry_url = src.entry_url;
		entry_request = src.entry_request;
		urls = src.urls == null ? null : new LinkedHashMap<>(src.urls);
		max_depth = src.max_depth;
		parameters_patterns = src.parameters_patterns == null ? null : new ArrayList<String>(src.parameters_patterns);
		inclusion_patterns = src.inclusion_patterns == null ? null : new ArrayList<String>(src.inclusion_patterns);
		exclusion_patterns = src.exclusion_patterns == null ? null : new ArrayList<String>(src.exclusion_patterns);
		remove_fragments = src.remove_fragments;
		browser_name = src.browser_name;
		browser_language = src.browser_language;
		browser_version = src.browser_version;
		browser_type = src.browser_type;
		javascript_enabled = src.javascript_enabled;
		download_images = src.download_images;
		web_security = src.web_security;
		robots_txt_enabled = src.robots_txt_enabled;
		robots_txt_useragent = src.robots_txt_useragent;
		proxy = src.proxy == null ? null : new ProxyDefinition(src.proxy);
		proxies = src.proxies == null ? null : new ArrayList<ProxyDefinition>(src.proxies);
		implicitly_wait = src.implicitly_wait;
		script_timeout = src.script_timeout;
		page_load_timeout = src.page_load_timeout;
		variables = src.variables == null ? null : new LinkedHashMap<String, String>(src.variables);
		if (src.scripts == null) {
			scripts = null;
		} else {
			scripts = new HashMap<>();
			for (Map.Entry<EventEnum, Script> entry : src.scripts.entrySet())
				scripts.put(entry.getKey(), new Script(entry.getValue()));
		}
	}

	public Object clone() {
		return new WebCrawlDefinition(this);
	}

	@JsonIgnore
	public WebCrawlDefinition setEntryUrl(final String entryUrl) {
		this.entry_url = entryUrl;
		return this;
	}

	@JsonIgnore
	public String getEntryUrl() {
		return this.entry_url;
	}

	@JsonIgnore
	public WebCrawlDefinition setEntryRequest(final WebRequestDefinition request) {
		this.entry_request = request;
		return this;
	}

	@JsonIgnore
	public WebRequestDefinition getEntryRequest() {
		return entry_request;
	}

	@JsonIgnore
	public WebCrawlDefinition setUrls(final LinkedHashMap<String, Integer> urls) {
		this.urls = urls;
		return this;
	}

	@JsonIgnore
	public LinkedHashMap<String, Integer> getUrls() {
		return this.urls;
	}

	@JsonIgnore
	public WebCrawlDefinition addUrl(final String url, final Integer depth) {
		if (urls == null)
			urls = new LinkedHashMap<>();
		urls.put(url, depth);
		return this;
	}

	@JsonIgnore
	public WebCrawlDefinition setMaxDepth(final Integer maxDepth) {
		this.max_depth = maxDepth;
		return this;
	}

	@JsonIgnore
	public Integer getMaxDepth() {
		return max_depth;
	}

	@JsonIgnore
	public WebCrawlDefinition setMaxUrlNumber(final Integer maxUrlNumber) {
		this.max_url_number = maxUrlNumber;
		return this;
	}

	@JsonIgnore
	public Integer getMaxUrlNumber() {
		return max_url_number;
	}

	@JsonIgnore
	public WebCrawlDefinition addParametersPattern(final String parametersPattern) {
		if (parameters_patterns == null)
			parameters_patterns = new ArrayList<>();
		parameters_patterns.add(parametersPattern);
		return this;
	}

	@JsonIgnore
	public Collection<String> getParametersPattern() {
		return parameters_patterns;
	}

	@JsonIgnore
	public WebCrawlDefinition addInclusionPattern(final String inclusionPattern) {
		if (inclusion_patterns == null)
			inclusion_patterns = new ArrayList<>();
		inclusion_patterns.add(inclusionPattern);
		return this;
	}

	@JsonIgnore
	public WebCrawlDefinition setInclusionPattern(final String inclusionPatternText) throws IOException {
		if (inclusionPatternText == null) {
			inclusion_patterns = null;
			return this;
		}
		inclusion_patterns = new ArrayList<>();
		StringUtils.linesCollector(inclusionPatternText, false, inclusion_patterns);
		return this;
	}

	@JsonIgnore
	public Collection<String> getInclusionPatterns() {
		return inclusion_patterns;
	}

	@JsonIgnore
	public WebCrawlDefinition setExclusionPattern(final String exclusionPatternText) throws IOException {
		if (exclusionPatternText == null) {
			exclusion_patterns = null;
			return this;
		}
		exclusion_patterns = new ArrayList<>();
		StringUtils.linesCollector(exclusionPatternText, false, exclusion_patterns);
		return this;
	}

	@JsonIgnore
	public WebCrawlDefinition addExclusionPattern(final String exclusionPattern) {
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
	public WebCrawlDefinition setRemoveFragments(final Boolean removeFragments) {
		this.remove_fragments = removeFragments;
		return this;
	}

	@JsonIgnore
	public Boolean getRemoveFragments() {
		return remove_fragments;
	}

	@JsonIgnore
	public WebCrawlDefinition setJavascriptEnabled(final Boolean javascriptEnabled) {
		this.javascript_enabled = javascriptEnabled;
		return this;
	}

	@JsonIgnore
	public Boolean getJavascriptEnabled() {
		return javascript_enabled;
	}

	@JsonIgnore
	public WebCrawlDefinition setDownloadImages(final Boolean downloadImages) {
		this.download_images = downloadImages;
		return this;
	}

	@JsonIgnore
	public Boolean getDownloadImages() {
		return download_images;
	}

	@JsonIgnore
	public WebCrawlDefinition setWebSecurity(final Boolean webSecurity) {
		this.web_security = webSecurity;
		return this;
	}

	@JsonIgnore
	public Boolean getWebSecurity() {
		return web_security;
	}

	@JsonIgnore
	public WebCrawlDefinition setRobotsTxtEnabled(final Boolean robotsTxtEnabled) {
		this.robots_txt_enabled = robotsTxtEnabled;
		return this;
	}

	@JsonIgnore
	public Boolean getRobotsTxtEnabled() {
		return robots_txt_enabled;
	}

	@JsonIgnore
	public WebCrawlDefinition setRobotsTxtUserAgent(final String robotsTxtUserAgent) {
		this.robots_txt_useragent = robotsTxtUserAgent;
		return this;
	}

	@JsonIgnore
	public String getRobotsTxtUserAgent() {
		return robots_txt_useragent;
	}

	@JsonIgnore
	public WebCrawlDefinition setBrowserType(final String browserType) {
		this.browser_type = BrowserDriverEnum.valueOf(browserType);
		return this;
	}

	@JsonIgnore
	public BrowserDriverEnum getBrowserType() {
		return browser_type;
	}

	@JsonIgnore
	public WebCrawlDefinition setBrowserName(final String browserName) {
		this.browser_name = browserName;
		return this;
	}

	@JsonIgnore
	public String getBrowserName() {
		return browser_name;
	}

	public WebCrawlDefinition setBrowserVersion(final String browserVersion) {
		this.browser_version = browserVersion;
		return this;
	}

	@JsonIgnore
	public String getBrowserVersion() {
		return browser_version;
	}

	@JsonIgnore
	public WebCrawlDefinition setBrowserLanguage(final String browserLanguage) {
		this.browser_language = browserLanguage;
		return this;
	}

	@JsonIgnore
	public String getBrowserLanguage() {
		return browser_language;
	}

	@JsonIgnore
	public ProxyDefinition newProxy() {
		if (proxy == null)
			proxy = new ProxyDefinition();
		return proxy;
	}

	@JsonIgnore
	public WebCrawlDefinition addProxy(final ProxyDefinition proxy) {
		if (proxy == null)
			return this;
		if (proxies == null)
			proxies = new ArrayList<>();
		proxies.add(proxy);
		return this;
	}

	@JsonIgnore
	public Collection<ProxyDefinition> getProxies() {
		return proxies;
	}

	@JsonIgnore
	public WebCrawlDefinition addVariable(final String name, final String value) {
		if (name == null || value == null)
			return this;
		if (variables == null)
			variables = new LinkedHashMap<>();
		variables.put(name, value);
		return this;
	}

	@JsonIgnore
	public WebCrawlDefinition setVariables(final Map<String, String> variables) {
		if (this.variables == null)
			this.variables = new LinkedHashMap<>();
		if (variables != null)
			this.variables.putAll(variables);
		return this;
	}

	@JsonIgnore
	public Map<String, String> getVariables() {
		return variables;
	}

	@JsonIgnore
	public WebCrawlDefinition addCookie(final String name, final String value) {
		if (name == null || value == null)
			return this;
		if (cookies == null)
			cookies = new LinkedHashMap<>();
		cookies.put(name, value);
		return this;
	}

	@JsonIgnore
	public Map<String, String> getCookies() {
		return cookies;
	}

	@JsonIgnore
	public WebCrawlDefinition setCookies(final Map<String, String> cookies) {
		if (this.cookies == null)
			this.cookies = new LinkedHashMap<>();
		if (cookies != null)
			this.cookies.putAll(cookies);
		return this;
	}

	@JsonIgnore
	public Script addScript(final String event, final String name) {
		if (scripts == null)
			scripts = new LinkedHashMap<>();
		Script script = new Script(name);
		scripts.put(EventEnum.valueOf(event), script);
		return script;
	}

	@JsonIgnore
	public Map<EventEnum, Script> getScripts() {
		return scripts;
	}

	@JsonIgnore
	public String urlEncode(final String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}

	@JsonIgnore
	public WebCrawlDefinition setImplicitlyWait(final Integer wait) {
		this.implicitly_wait = wait;
		return this;
	}

	@JsonIgnore
	public Integer getImplicitlyWait() {
		return implicitly_wait;
	}

	@JsonIgnore
	public WebCrawlDefinition setScriptTimeout(final Integer timeout) {
		this.script_timeout = timeout;
		return this;
	}

	@JsonIgnore
	public Integer getScriptTimeout() {
		return script_timeout;
	}

	@JsonIgnore
	public WebCrawlDefinition setPageLoadTimeout(Integer timeout) {
		this.page_load_timeout = timeout;
		return this;
	}

	@JsonIgnore
	public Integer getPageLoadTimeout() {
		return page_load_timeout;
	}

	@JsonIgnore
	public static WebCrawlDefinition newInstance(final String json) throws IOException {
		return JsonMapper.MAPPER.readValue(json, WebCrawlDefinition.class);
	}

}
