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
	 * A set of URL to crawl
	 */
	public List<String> urls = null;

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
		public Object clone() {
			return new Script(this);
		}

		public Script addVariable(String name, String value) {
			if (variables == null)
				variables = new HashMap<String, String>();
			variables.put(name, value);
			return this;
		}

	}

	public WebCrawlDefinition() {
	}

	protected WebCrawlDefinition(WebCrawlDefinition src) {
		entry_url = src.entry_url;
		urls = src.urls == null ? null : new ArrayList<String>(src.urls);
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
			scripts = new HashMap<EventEnum, Script>();
			for (Map.Entry<EventEnum, Script> entry : src.scripts.entrySet())
				scripts.put(entry.getKey(), new Script(entry.getValue()));
		}
	}

	public Object clone() {
		return new WebCrawlDefinition(this);
	}

	public WebCrawlDefinition setEntry_url(String entry_url) {
		this.entry_url = entry_url;
		return this;
	}

	public WebCrawlDefinition setUrls(List<String> urls) {
		this.urls = urls;
		return this;
	}

	public WebCrawlDefinition addUrl(String url) {
		if (urls == null)
			urls = new ArrayList<String>();
		urls.add(url);
		return this;
	}

	public WebCrawlDefinition setMax_depth(Integer max_depth) {
		this.max_depth = max_depth;
		return this;
	}

	public WebCrawlDefinition setMax_url_number(Integer max_url_number) {
		this.max_url_number = max_url_number;
		return this;
	}

	public WebCrawlDefinition addParameters_pattern(String parameters_pattern) {
		if (parameters_patterns == null)
			parameters_patterns = new ArrayList<String>();
		parameters_patterns.add(parameters_pattern);
		return this;
	}

	public WebCrawlDefinition addInclusion_pattern(String inclusion_pattern) {
		if (inclusion_patterns == null)
			inclusion_patterns = new ArrayList<String>();
		inclusion_patterns.add(inclusion_pattern);
		return this;
	}

	public WebCrawlDefinition setInclusion_pattern(String inclusion_pattern_text) throws IOException {
		if (inclusion_pattern_text == null) {
			inclusion_patterns = null;
			return this;
		}
		inclusion_patterns = new ArrayList<String>();
		StringUtils.linesCollector(inclusion_pattern_text, false, inclusion_patterns);
		return this;
	}

	public WebCrawlDefinition setExclusion_pattern(String exclusion_pattern_text) throws IOException {
		if (exclusion_pattern_text == null) {
			exclusion_patterns = null;
			return this;
		}
		exclusion_patterns = new ArrayList<String>();
		StringUtils.linesCollector(exclusion_pattern_text, false, exclusion_patterns);
		return this;
	}

	public WebCrawlDefinition addExclusion_pattern(String exclusion_pattern) {
		if (exclusion_patterns == null)
			exclusion_patterns = new ArrayList<String>();
		exclusion_patterns.add(exclusion_pattern);
		return this;
	}

	public WebCrawlDefinition setRemove_fragments(Boolean remove_fragments) {
		this.remove_fragments = remove_fragments;
		return this;
	}

	public WebCrawlDefinition setJavascript_enabled(Boolean javascript_enabled) {
		this.javascript_enabled = javascript_enabled;
		return this;
	}

	public WebCrawlDefinition setDownload_images(Boolean download_images) {
		this.download_images = download_images;
		return this;
	}

	public WebCrawlDefinition setWeb_security(Boolean web_security) {
		this.web_security = web_security;
		return this;
	}

	public WebCrawlDefinition setRobots_txt_enabled(Boolean robots_txt_enabled) {
		this.robots_txt_enabled = robots_txt_enabled;
		return this;
	}

	public WebCrawlDefinition setRobots_txt_useragent(String robots_txt_useragent) {
		this.robots_txt_useragent = robots_txt_useragent;
		return this;
	}

	public WebCrawlDefinition setBrowser_type(String browser_type) {
		this.browser_type = BrowserDriverEnum.valueOf(browser_type);
		return this;
	}

	public WebCrawlDefinition setBrowser_name(String browser_name) {
		this.browser_name = browser_name;
		return this;
	}

	public WebCrawlDefinition setBrowser_version(String browser_version) {
		this.browser_version = browser_version;
		return this;
	}

	public WebCrawlDefinition setBrowser_language(String browser_language) {
		this.browser_language = browser_language;
		return this;
	}

	@JsonIgnore
	public ProxyDefinition newProxy() {
		if (proxy == null)
			proxy = new ProxyDefinition();
		return proxy;
	}

	public WebCrawlDefinition addProxy(ProxyDefinition proxy) {
		if (proxy == null)
			return this;
		if (proxies == null)
			proxies = new ArrayList<ProxyDefinition>();
		proxies.add(proxy);
		return this;
	}

	public WebCrawlDefinition addVariable(String name, String value) {
		if (name == null || value == null)
			return this;
		if (variables == null)
			variables = new LinkedHashMap<String, String>();
		variables.put(name, value);
		return this;
	}

	public WebCrawlDefinition addCookie(String name, String value) {
		if (name == null || value == null)
			return this;
		if (cookies == null)
			cookies = new LinkedHashMap<String, String>();
		cookies.put(name, value);
		return this;
	}

	public Script addScript(String event, String name) {
		if (scripts == null)
			scripts = new LinkedHashMap<EventEnum, Script>();
		Script script = new Script(name);
		scripts.put(EventEnum.valueOf(event), script);
		return script;
	}

	public String urlEncode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}

	public WebCrawlDefinition setImplicitly_wait(Integer wait) {
		this.implicitly_wait = wait;
		return this;
	}

	public WebCrawlDefinition setScript_timeout(Integer timeout) {
		this.script_timeout = timeout;
		return this;
	}

	public WebCrawlDefinition setPage_load_timeout(Integer timeout) {
		this.page_load_timeout = timeout;
		return this;
	}

	public static WebCrawlDefinition newInstance(String json) throws IOException {
		return JsonMapper.MAPPER.readValue(json, WebCrawlDefinition.class);
	}

}
