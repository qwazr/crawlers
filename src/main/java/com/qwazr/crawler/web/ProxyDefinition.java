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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProxyDefinition {

	final public Boolean enabled;

	/**
	 * the proxy host for FTP connections, expected format is
	 * <code>hostname:1234</code>
	 */
	@JsonProperty("ftp_proxy")
	final public String ftpProxy;

	/**
	 * the proxy host for HTTP connections, expected format is
	 * <code>hostname:1234</code>
	 */
	@JsonProperty("http_proxy")
	final public String httpProxy;

	/**
	 * The proxy bypass (noproxy) addresses
	 */
	@JsonProperty("no_proxy")
	final public String noProxy;

	/**
	 * the proxy host for SSL connections, expected format is
	 * <code>hostname:1234</code>
	 */
	@JsonProperty("ssl_proxy")
	final public String sslProxy;

	/**
	 * the proxy host for SOCKS v5 connections, expected format is
	 * <code>hostname:1234</code>
	 */
	@JsonProperty("socks_proxy")
	final public String socksProxy;

	/**
	 * the SOCKS proxy's username
	 */
	@JsonProperty("socks_username")
	final public String socksUsername;

	/**
	 * the SOCKS proxy's password
	 */
	@JsonProperty("socks_password")
	final public String socksPassword;

	/**
	 * Specifies the URL to be used for proxy auto-configuration. Expected
	 * format is <code>http://hostname.com:1234/pacfile</code>
	 */
	@JsonProperty("proxy_autoconfig_url")
	final public String proxyAutoconfigUrl;

	public ProxyDefinition(@JsonProperty("enabled") Boolean enabled, @JsonProperty("ftp_proxy") String ftpProxy,
			@JsonProperty("http_proxy") String httpProxy, @JsonProperty("no_proxy") String noProxy,
			@JsonProperty("ssl_proxy") String sslProxy, @JsonProperty("socks_proxy") String socksProxy,
			@JsonProperty("socks_username") String socksUsername, @JsonProperty("socks_password") String socksPassword,
			@JsonProperty("proxy_autoconfig_url") String proxyAutoconfigUrl) {
		this.enabled = enabled;
		this.ftpProxy = ftpProxy;
		this.httpProxy = httpProxy;
		this.noProxy = noProxy;
		this.sslProxy = sslProxy;
		this.socksProxy = socksProxy;
		this.socksUsername = socksUsername;
		this.socksPassword = socksPassword;
		this.proxyAutoconfigUrl = proxyAutoconfigUrl;
	}

	protected ProxyDefinition(Builder builder) {
		enabled = builder.enabled;
		ftpProxy = builder.ftpProxy;
		httpProxy = builder.httpProxy;
		noProxy = builder.noProxy;
		sslProxy = builder.sslProxy;
		socksProxy = builder.socksProxy;
		socksUsername = builder.socksUsername;
		socksPassword = builder.socksPassword;
		proxyAutoconfigUrl = builder.proxyAutoconfigUrl;
	}

	public static Builder of() {
		return new Builder();
	}

	public static class Builder {

		private Boolean enabled;

		private String ftpProxy;

		private String httpProxy;

		private String noProxy;

		private String sslProxy;

		private String socksProxy;

		private String socksUsername;

		private String socksPassword;

		private String proxyAutoconfigUrl;

		public Builder enabled(Boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder ftpProxy(String ftpProxy) {
			this.ftpProxy = ftpProxy;
			return this;
		}

		public Builder httpProxy(String httpProxy) {
			this.httpProxy = httpProxy;
			return this;
		}

		public Builder noProxy(String noProxy) {
			this.noProxy = noProxy;
			return this;
		}

		public Builder sslProxy(String sslProxy) {
			this.sslProxy = sslProxy;
			return this;
		}

		public Builder socksProxy(String socksProxy) {
			this.socksProxy = socksProxy;
			return this;
		}

		public Builder socksUsername(String socksUsername) {
			this.socksUsername = socksUsername;
			return this;
		}

		public Builder socksPassword(String socksPassword) {
			this.socksPassword = socksPassword;
			return this;
		}

		public Builder proxyAutoconfigUrl(String proxyAutoconfigUrl) {
			this.proxyAutoconfigUrl = proxyAutoconfigUrl;
			return this;
		}

		public ProxyDefinition build() {
			return new ProxyDefinition(this);
		}
	}

}