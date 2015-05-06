/**
 * Copyright 2014-2015 OpenSearchServer Inc.
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
package com.qwazr.crawler.web.driver;

import java.lang.reflect.InvocationTargetException;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qwazr.crawler.web.service.WebCrawlDefinition;

/**
 * This class is responsible of creating a WebDriver build the capabilities by
 * reading the crawl definition (CrawlJson).
 */
public class BrowserDriverBuilder {

	private final WebCrawlDefinition crawlDefinition;

	public BrowserDriverBuilder(WebCrawlDefinition crawlDefinition)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		this.crawlDefinition = crawlDefinition;
	}

	private DesiredCapabilities checkCapabilities(
			DesiredCapabilities capabilities) {
		if (capabilities == null)
			capabilities = new DesiredCapabilities();
		return capabilities;
	}

	public BrowserDriver<?> build() throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		BrowserDriverEnum browserType = BrowserDriverEnum.html_unit;
		DesiredCapabilities capabilities = null;
		if (crawlDefinition != null) {

			// Setup the proxy
			if (crawlDefinition.proxy != null) {
				capabilities = checkCapabilities(capabilities);
				org.openqa.selenium.Proxy proxy = new Proxy();
				if (crawlDefinition.proxy.http_proxy != null)
					proxy.setHttpProxy(crawlDefinition.proxy.http_proxy);
				if (crawlDefinition.proxy.ftp_proxy != null)
					proxy.setFtpProxy(crawlDefinition.proxy.ftp_proxy);
				if (crawlDefinition.proxy.ssl_proxy != null)
					proxy.setSslProxy(crawlDefinition.proxy.ssl_proxy);
				if (crawlDefinition.proxy.socks_proxy != null)
					proxy.setSocksProxy(crawlDefinition.proxy.socks_proxy);
				if (crawlDefinition.proxy.socks_username != null)
					proxy.setSocksUsername(crawlDefinition.proxy.socks_username);
				if (crawlDefinition.proxy.socks_password != null)
					proxy.setSocksPassword(crawlDefinition.proxy.socks_password);
				if (crawlDefinition.proxy.no_proxy != null)
					proxy.setNoProxy(crawlDefinition.proxy.no_proxy);
				if (crawlDefinition.proxy.proxy_autoconfig_url != null)
					proxy.setProxyAutoconfigUrl(crawlDefinition.proxy.proxy_autoconfig_url);
				capabilities.setCapability(CapabilityType.PROXY,
						crawlDefinition.proxy);
			}

			// Setup the language
			if (crawlDefinition.browser_language != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities.setCapability(
						AdditionalCapabilities.QWAZR_BROWER_LANGUAGE,
						crawlDefinition.browser_language);
			}

			// Choose a browser type
			if (crawlDefinition.browser_type != null)
				browserType = crawlDefinition.browser_type;

			// Javascript capability
			if (crawlDefinition.javascript_enabled != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities
						.setJavascriptEnabled(crawlDefinition.javascript_enabled);
			}
		}
		return browserType.getNewInstance(capabilities);
	}
}
