/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.crawler.web.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.qwazr.crawler.web.driver.BrowserDriverEnum;

@JsonInclude(Include.NON_EMPTY)
public class WebCrawlDefinition {

	/**
	 * The entry point URL of the crawl.
	 */
	public String entry_url = null;

	/**
	 * The maximum depth of the crawl.
	 */
	public Integer max_depth = null;

	/**
	 * A list of regular expression patterns. Any parameters which matches is
	 * removed.
	 */
	public List<String> parameters_patterns = null;

	/**
	 * A list of regular expression patterns. An URL is crawled only if it
	 * matches any pattern.
	 */
	public List<String> inclusion_patterns = null;

	/**
	 * A list of regular expression patterns. An URL will not be crawled if it
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
	 * Enable or disable javascript
	 */
	public Boolean javascript_enabled = null;

	/**
	 * The proxy definition
	 */
	public ProxyDefinition proxy = null;

	@JsonInclude(Include.NON_EMPTY)
	public class ProxyDefinition {

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
		public String socks_password = null;

		/**
		 * Specifies the URL to be used for proxy auto-configuration. Expected
		 * format is <code>http://hostname.com:1234/pacfile</code>
		 */
		public String proxy_autoconfig_url = null;
	}

	/**
	 * The global variables shared by all the scripts.
	 */
	public Map<String, String> variables = null;

	/**
	 * A list of scripts paths mapped with the events which fire the scripts.
	 */
	public Map<EventEnum, Script> scripts = null;

	public static enum EventEnum {

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
	public static class Script {

		/**
		 * The path to the script
		 */
		public String name = null;

		/**
		 * The local variables passed to the script
		 */
		public Map<String, String> variables = null;

		public Script() {
			this(null);
		}

		public Script(String name) {
			this.name = name;
		}

		public Script addVariable(String name, String value) {
			if (variables == null)
				variables = new HashMap<String, String>();
			variables.put(name, value);
			return this;
		}
	}

	public WebCrawlDefinition setEntry_url(String entry_url) {
		this.entry_url = entry_url;
		return this;
	}

	public WebCrawlDefinition setMax_depth(Integer max_depth) {
		this.max_depth = max_depth;
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

	public WebCrawlDefinition setBrowser_type(String browser_type) {
		this.browser_type = BrowserDriverEnum.valueOf(browser_type);
		return this;
	}

	public WebCrawlDefinition setBrowser_language(String browser_language) {
		this.browser_language = browser_language;
		return this;
	}

	public WebCrawlDefinition addVariable(String name, String value) {
		if (variables == null)
			variables = new HashMap<String, String>();
		variables.put(name, value);
		return this;
	}

	public Script addScript(String event, String name) {
		if (scripts == null)
			scripts = new HashMap<EventEnum, Script>();
		Script script = new Script(name);
		scripts.put(EventEnum.valueOf(event), script);
		return script;
	}

	public String urlEncode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}

}
